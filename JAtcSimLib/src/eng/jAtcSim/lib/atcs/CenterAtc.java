/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.world.Navaid;

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
        Airplane p = m.getSource();
        SpeechList spchs = m.getContent();

        // only Good-Day speeches are available from planes, other are ignored
        if (spchs.containsType(GoodDayNotification.class) == false)
          continue;

        SpeechList<IFromAtc> newCmds = new SpeechList<>();

        newCmds.add(new RadarContactConfirmationNotification());

        if (p.isDeparture()) {
          // new departure

          // order to climb
          newCmds.add(
            new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(p)));

          // order to continue after last fix
          Navaid n = p.getDepartureLastNavaid();
          if (n != null) {
            newCmds.add(new AfterNavaidCommand(n));
            newCmds.add(new ChangeHeadingCommand());
          }
        } // if isDeparture

        Message msg;
        msg = new Message(
            this,
            p,
            newCmds);
        Acc.messenger().send(msg);
        recorder.logMessage(msg);

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
        Message msg = new Message(this, p,
            new SpeechList<>(new ContactCommand(eType.app))
            );
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
