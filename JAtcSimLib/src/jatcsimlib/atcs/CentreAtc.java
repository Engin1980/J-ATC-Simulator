/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.Message;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class CentreAtc extends ComputerAtc {

  private AirplaneList departingConfirmedList = new AirplaneList();
  private AirplaneList departingList = new AirplaneList();

  private final AirplaneList arrivingNewList = new AirplaneList();
  private final AirplaneList arrivingForApp = new AirplaneList();

  public CentreAtc(String areaIcao) {
    super(Atc.eType.ctr, areaIcao);
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    if (plane.isDeparture()) {
      throw new ERuntimeException("Departure plane cannot be registered using this method.");
    } else {
      arrivingNewList.add(plane);
      Acc.messenger().addMessage(null);
    }

  }

  public void elapseSecond() {
    List<Message> msgs = Acc.messenger().getMy(this);
    List<Message> tmp = new LinkedList<>();

    // odstrani nezname
    for (Message m : msgs) {
      if (Atc.UNRECOGNIZED.equals(m.tryGetText())) {
        Acc.messenger().remove(m);
        msgs.remove(m);
      }
    }

    // CTR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      Acc.messenger().addMessage(this, Acc.atcApp(), p.getSqwk().toString());
      arrivingNewList.remove(p);
      arrivingForApp.add(p);
    }
    plns.clear();

    // CTR -> APP, potvrzene od APP
    for (Message m : msgs) {
      if (m.source != Acc.atcApp()) {
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
        // CTR predava na APP
        arrivingForApp.remove(p); // bude odstraneno
        Acc.messenger().addMessage(
            new Message(this, p, new ContactCommand(eType.app)));
        tmp.add(m);
      }
    }
    msgs.removeAll(tmp);
  }

  private AirplaneList getPlanesReadyForApp() {
    AirplaneList ret = new AirplaneList();
    // arriving pozadat o predani CTR na APP
    for (Airplane p : arrivingNewList) {
      if (p.getAltitude() < 16E3) {
        ret.add(p);
      }
    }
    return ret;
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isDeparture() == false)
      return false;
    if (p.getAtc() != Acc.atcApp())
      return false;
    if (p.getAltitude() < Acc.airport().getMinCtrAtcAltitude())
      return false;
    return true;
  }
}
