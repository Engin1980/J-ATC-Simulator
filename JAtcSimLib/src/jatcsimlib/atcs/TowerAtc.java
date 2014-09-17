/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.ClearedForTakeoffCommand;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.messaging.Message;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class TowerAtc extends ComputerAtc {

  private final AirplaneList arrivingFromApp = new AirplaneList();
  private final AirplaneList arriving = new AirplaneList();

  private final AirplaneList readyForTakeOff = new AirplaneList();
  private final AirplaneList readyWaitingForApp = new AirplaneList();
  private final AirplaneList departed = new AirplaneList();

  private final WaitingList waitingRequestsList = new WaitingList();

  public TowerAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public void elapseSecond() {
    List<Message> msgs = Acc.messenger().getMy(this, true);
    List<Message> tmp = new LinkedList<>();
    
    if (msgs.isEmpty() == false){
      System.out.println("## PP");
    }

    esRequestPlaneSwitchFromApp();

    // TWR -> APP, potvrzene od APP
    for (Message m : msgs) {
      if (m.source != Acc.atcApp()) {
        tmp.add(m);
        continue;
      }
      Airplane p = m.getAsPlaneSwitchMessage().plane;
      if (readyWaitingForApp.contains(p) == false) {
        p = null;
      }

      if (p == null) {
        // APP predava na TWR, muze?
        p = m.getAsPlaneSwitchMessage().plane;
        if (canIAcceptFromApp(p)) {
          arrivingFromApp.add(p);
          Acc.messenger().addMessage(this, Acc.atcApp(),
              new PlaneSwitchMessage(p, " accepted"));
          Acc.messenger().remove(m);
        } else {
          Acc.messenger().addMessage(this, Acc.atcApp(),
              new PlaneSwitchMessage(p, " refused. Not in my coverage."));
          Acc.messenger().remove(m);
        }

      } else {
        // potvrzene od APP, TWR predava na APP
        readyWaitingForApp.remove(p); // bude odstraneno
        waitingRequestsList.remove(p);
        Acc.messenger().addMessage(this, p, new ClearedForTakeoffCommand());
        Acc.messenger().addMessage(this, p, new ContactCommand(eType.app));
        tmp.add(m);
      }
    }
    msgs.removeAll(tmp);
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isDeparture()) {
      return false;
    }
    if (Acc.atcApp().isControllingAirplane(p) == false) {
      return false;
    }
    if (p.getAltitude() < super.acceptAltitude) {
      return false;
    }
    double dist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
    if (dist > 15) {
      return false;
    }
    return true;
  }

  private void esRequestPlaneSwitchFromApp() {
    // CTR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      Acc.messenger().addMessage(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you"));
      readyForTakeOff.remove(p);
      readyWaitingForApp.add(p);
      waitingRequestsList.add(p);
    }
    plns.clear();

    // opakovani starych zadosti
    List<Airplane> awaitings = waitingRequestsList.getAwaitings();
    for (Airplane p : awaitings) {
      Acc.messenger().addMessage(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you (repeated)"));
    }
  }

  private AirplaneList getPlanesReadyForApp() {
//    AirplaneList ret = new AirplaneList();
//    // arriving pozadat o predani CTR na APP
//    for (Airplane p : arrivingNewList) {
//      if (p.getAltitude() < super.releaseAltitude) {
//        ret.add(p);
//      }
//    }
    return readyForTakeOff;
  }

}
