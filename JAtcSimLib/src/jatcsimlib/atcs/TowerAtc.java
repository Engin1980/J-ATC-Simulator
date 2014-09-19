/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.AfterAltitudeCommand;
import jatcsimlib.commands.ClearedForTakeoffCommand;
import jatcsimlib.commands.CommandList;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.messaging.GoingAroundStringMessage;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.StringMessage;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class TowerAtc extends ComputerAtc {

  private final WaitingList waitingRequestsList = new WaitingList();

  public TowerAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  protected void _elapseSecond() {
    List<Message> msgs = Acc.messenger().getMy(this, true);

    if (msgs.isEmpty() == false) {
      System.out.println("## PP");
    }

    esRequestPlaneSwitchFromApp();

    for (Message m : msgs) {
      if (m.source instanceof Airplane) {
        if (m.content instanceof GoingAroundStringMessage) {
          // predavame na APP
          super.requestSwitch((Airplane) m.source);
          waitingRequestsList.add((Airplane) m.source);
        }
      } else if (m.source != Acc.atcApp()) {
        continue;
      }

      Airplane p = m.getAsPlaneSwitchMessage().plane;
      if (waitingRequestsList.contains(p) == false) {
        p = null;
      }

      if (p == null) {
        // APP -> TWR, muze?
        p = m.getAsPlaneSwitchMessage().plane;
        if (canIAcceptFromApp(p)) {
          super.confirmSwitch(p);
        } else {
          super.refuseSwitch(p);
        }

      } else {
        // potvrzene od APP, TWR predava na APP
        waitingRequestsList.remove(p);
        CommandList cmdList = new CommandList();
        cmdList.add(new ClearedForTakeoffCommand());
        cmdList.add(new AfterAltitudeCommand(Acc.airport().getAltitude() + 200));
        cmdList.add(new ContactCommand(eType.app));
        Acc.messenger().addMessage(this, p, cmdList);
      }
    }
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
    // TWR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      Acc.messenger().addMessage(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you"));

      getPrm().requestSwitch(this, Acc.atcApp(), p);

      waitingRequestsList.add(p);
    }

    // opakovani starych zadosti
    List<Airplane> awaitings = waitingRequestsList.getAwaitings();
    for (Airplane p : awaitings) {
      Acc.messenger().addMessage(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you (repeated)"));
    }
  }

  private AirplaneList getPlanesReadyForApp() {
    AirplaneList ret = new AirplaneList();

    for (Airplane p : getPrm().getPlanes(this)) {
      if (p.isDeparture() == false) {
        continue;
      }
      if (getPrm().isToSwitch(p)) {
        continue;
      }

      ret.add(p);
    }

    return ret;
  }

}
