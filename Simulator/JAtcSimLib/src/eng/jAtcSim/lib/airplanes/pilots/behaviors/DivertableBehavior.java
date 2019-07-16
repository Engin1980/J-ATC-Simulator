package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IDivertModuleRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;

public abstract class DivertableBehavior extends Behavior {

  private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};

  private void adviceDivertTimeIfRequested(IPilot5Behavior pilot) {
    IDivertModuleRO di = pilot.getDivertModule();
    if (di == null) return;

    for (int dit : divertAnnounceTimes) {
      int minLeft = di.getMinutesLeft();
      if (di.getLastAnnouncedMinute() > dit && minLeft < dit) {
        pilot.say(
            new DivertTimeNotification(di.getMinutesLeft()));
        pilot.setLastAnnouncedMinuteForDivert(minLeft);
        break;
      }
    }
  }

  private boolean divertIfRequested(IPilot5Behavior pilot) {
    IDivertModuleRO di = pilot.getDivertModule();
    if (di != null && di.getMinutesLeft() <= 0) {
      pilot.processDivert();
      return true;
    } else {
      return false;
    }
  }

  protected boolean processDivertManagement(IPilot5Behavior pilot){
    if (divertIfRequested(pilot) == false){
      adviceDivertTimeIfRequested(pilot);
      return false;
    } else
      return true;
  }
}
