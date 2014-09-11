/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ETime;
import jatcsimlib.messaging.Message;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marek
 */
public class CentreAtc extends ComputerAtc {

  private final AirplaneList departingConfirmedList = new AirplaneList();
  private final AirplaneList departingList = new AirplaneList();

  private final AirplaneList arrivingNewList = new AirplaneList();
  private final AirplaneList arrivingForApp = new AirplaneList();
  
  private final Map<Squawk, ETime> waitingRequestsList = new HashMap<>();

  public CentreAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    if (plane.isDeparture()) {
      throw new ERuntimeException("Departure plane cannot be registered using this method.");
    } else {
      arrivingNewList.add(plane);      
    }
  }

  public void elapseSecond() {
    List<Message> msgs = Acc.messenger().getMy(this, true);
    List<Message> tmp = new LinkedList<>();
    
    esRemoveInvalidMessages(msgs);
    
    esRequestPlaneSwitchFromApp();

    // CTR -> APP, potvrzene od APP
    for (Message m : msgs) {
      if (m.source != Acc.atcApp()) {
        tmp.add(m);
        continue;
      }
      String sqwk = m.tryGetText();
      Airplane p = arrivingForApp.tryGetBySqwk(sqwk);

      if (p == null) {
        // APP predava na CTR, muze?
        p = Acc.planes().tryGetBySqwk(sqwk);
        if (p == null) {
          Acc.messenger().addMessage(this, Acc.atcApp(), sqwk + " fail - invalid SQWK");
          Acc.messenger().remove(m);
        } else {
          if (canIAcceptFromApp(p)) {
            departingConfirmedList.add(p);
            Acc.messenger().addMessage(this, Acc.atcApp(), p.getSqwk().toString());
            Acc.messenger().remove(m);
          } else {
            Acc.messenger().addMessage(this, Acc.atcApp(), sqwk + " fail - not in CTR coverage");
            Acc.messenger().remove(m);
          }
        }

      } else {
        // ptvrzene od APP, CTR predava na APP
        arrivingForApp.remove(p); // bude odstraneno
        waitingRequestsList.remove(p.getSqwk());
        Acc.messenger().addMessage(
            new Message(this, p, new ContactCommand(eType.app)));
        tmp.add(m);
      }
    }
    msgs.removeAll(tmp);
  }

  private void esRemoveInvalidMessages(List<Message> msgs) {
    // odstrani nezname
    for (Message m : msgs) {
      if (Atc.UNRECOGNIZED.equals(m.tryGetText())) {
        Acc.messenger().remove(m);
        msgs.remove(m);
      }
    }
  }

  private void esRequestPlaneSwitchFromApp() {
    // CTR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      Acc.messenger().addMessage(this, Acc.atcApp(), p.getSqwk().toString());
      arrivingNewList.remove(p);
      arrivingForApp.add(p);
      waitingRequestsList.put(p.getSqwk(), Acc.now());
    }
    plns.clear();
    
    // opakovani starych zadosti
    ETime old = Acc.now().addSeconds(-20);
    for (Squawk k : waitingRequestsList.keySet()){
      if (waitingRequestsList.get(k).isBefore(old)){
        Acc.messenger().addMessage(this, Acc.atcApp(), k.toString());
      }
    }
  }

  private AirplaneList getPlanesReadyForApp() {
    AirplaneList ret = new AirplaneList();
    // arriving pozadat o predani CTR na APP
    for (Airplane p : arrivingNewList) {
      if (p.getAltitude() < super.releaseAltitude) {
        ret.add(p);
      }
    }
    return ret;
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isDeparture() == false)
      return false;
    if (Acc.atcApp().isControllingAirplane(p) == false)
      return false;
    if (p.getAltitude() < super.acceptAltitude)
      return false;
    return true;
  }

  @Override
  public boolean isControllingAirplane(Airplane plane) {
    if (departingConfirmedList.contains(plane))
      return true;
    else if (departingList.contains(plane))
      return true;
    else if (arrivingNewList.contains(plane))
      return true;
    
    return false;
  }
}
