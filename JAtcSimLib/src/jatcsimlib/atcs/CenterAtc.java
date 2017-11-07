/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.Message;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import jatcsimlib.speaking.fromAtc.commands.ContactCommand;
import jatcsimlib.speaking.fromAirplane.notifications.GoodDayNotification;
import jatcsimlib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.util.List;

/**
 *
 * @author Marek
 */
public class CenterAtc extends ComputerAtc {

  public CenterAtc(AtcTemplate template) {
    super(template);
  }

  @Override
  public void init() {
  }

  @Override
  protected void _registerNewPlane(Airplane plane) {
    if (plane.isDeparture()) {
      throw new ERuntimeException("Departure plane cannot be registered using this method.");
    }
  }

  @Override
  protected void _elapseSecond() {
    List<Message> msgs = Acc.messenger().getByTarget(this,true);

    esRequestPlaneSwitchFromApp();

    for (Message m : msgs) {
      recorder.logMessage(m); // incoming speech

      if (m.isSourceOfType(Airplane.class)) {
        
        SpeechList cmds = m.getContent();
        
        if (cmds.containsType(GoodDayNotification.class) == false)
          continue;
        
        Airplane p = m.getSource();
        Message msg;

        msg = new Message(
            this,
            p,
            new RadarContactConfirmationNotification());
        Acc.messenger().send(msg);
        recorder.logMessage(msg);

        if (p.isDeparture()) {
          msg = new Message(
            this,
            p,
            new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(p)));
          Acc.messenger().send(msg);
          recorder.logMessage(msg);
        }
      }
      if (m.getSource() != Acc.atcApp()) {
        continue;
      }

      Airplane p = m.<PlaneSwitchMessage>getContent().plane;
      if (waitingRequestsList.contains(p) == false) {
        p = null;
      }

      if (p == null) {
        // APP -> CTR, muze?       

        p = m.<PlaneSwitchMessage>getContent().plane;
        if (canIAcceptFromApp(p)) {
          super.confirmSwitch(p);
          super.approveSwitch(p);
        } else {
          super.refuseSwitch(p);
        }

      } else {
        // CTR -> APP, potvrzene od APP
        waitingRequestsList.remove(p);
        super.approveSwitch(p);
        Message msg = new Message(this, p, new ContactCommand(eType.app));
        Acc.messenger().send(msg);
        recorder.logMessage(msg);
      }
    }
  }

  private void esRequestPlaneSwitchFromApp() {
    // CTR -> APP, zadost na APP
    AirplaneList plns = getPlanesReadyForApp();
    for (Airplane p : plns) {
      super.requestSwitch(p);
      waitingRequestsList.add(p);
    }
  }

  private AirplaneList getPlanesReadyForApp() {
    AirplaneList ret = new AirplaneList();
    // arriving pozadat o predani CTR na APP
    for (Airplane p : getPrm().getPlanes(this)) {
      if (p.isDeparture()) {
        continue;
      }
      if (getPrm().isAskedToSwitch(p)) {
        continue;
      }
      if (p.getAltitude() < super.releaseAltitude) {
        ret.add(p);
      }
    }
    return ret;
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isArrival()) {
      return false;
    }
    if (Acc.atcApp().isControllingAirplane(p) == false) {
      return false;
    }
    if (p.getAltitude() < super.acceptAltitude) {
      return false;
    }
    return true;
  }

  private int getDepartureRandomTargetAltitude(Airplane p) {
    int ret = Acc.rnd().nextInt(20, p.getType().maxAltitude / 1000);
    ret = ret * 1000;
    return ret;
  }

}
