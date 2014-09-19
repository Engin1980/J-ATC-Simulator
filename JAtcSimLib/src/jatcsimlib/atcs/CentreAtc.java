/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.messaging.Message;
import java.util.LinkedList;
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
        } else {
          super.refuseSwitch(p);
        }

      } else {
        // CTR -> APP, potvrzene od APP
        waitingRequestsList.remove(p);
        Acc.messenger().addMessage(this, p, new ContactCommand(eType.app));
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
      if (getPrm().isToSwitch(p)) {
        continue;
      }
      if (p.getAltitude() < super.releaseAltitude) {
        ret.add(p);
      }
    }
    return ret;
  }

  private boolean canIAcceptFromApp(Airplane p) {
    if (p.isDeparture() == false) {
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

}
