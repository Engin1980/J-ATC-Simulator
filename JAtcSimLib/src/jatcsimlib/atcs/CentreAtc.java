/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.Message;
import java.util.List;

/**
 *
 * @author Marek
 */
public class CentreAtc extends ComputerAtc {

  public CentreAtc(AtcTemplate template) {
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
    List<Message> msgs = Acc.messenger().getMy(this, true);

    esRequestPlaneSwitchFromApp();

    for (Message m : msgs) {
      recorder.logMessage(m); // incoming message
      
      if (m.source instanceof Airplane) {
        Airplane p = (Airplane) m.source;
        if (p.isDeparture()) {
          Message n = Message.create(
            this,
            p,
            new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.climb, getDepartureRandomTargetAltitude(p)));
          Acc.messenger().addMessage(n);
          recorder.logMessage(n);
        }
      }
      if (m.source != Acc.atcApp()) {
        continue;
      }

      Airplane p = m.getAsPlaneSwitchMessage().plane;
      if (waitingRequestsList.contains(p) == false) {
        p = null;
      }

      if (p == null) {
        // APP -> CTR, muze?       

        p = m.getAsPlaneSwitchMessage().plane;
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
        Message n = Message.create(this, p, new ContactCommand(eType.app));
        Acc.messenger().addMessage(n);
        recorder.logMessage(n);
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
