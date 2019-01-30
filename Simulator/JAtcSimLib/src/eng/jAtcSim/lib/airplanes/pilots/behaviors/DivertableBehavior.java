package eng.jAtcSim.lib.airplanes.pilots.behaviors;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;

public abstract class DivertableBehavior extends Behavior {

  private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};

  private void adviceDivertTimeIfRequested(IPilot4Behavior pilot) {
    Pilot.DivertInfo di = pilot.getDivertInfo();
    if (di == null) return;

    for (int dit : divertAnnounceTimes) {
      int minLeft = di.getMinutesLeft();
      if (di.lastAnnouncedMinute > dit && minLeft < dit) {
        pilot.say(
            new DivertTimeNotification(di.getMinutesLeft()));
        di.lastAnnouncedMinute = minLeft;
        break;
      }
    }
  }

  private boolean divertIfRequested(IPilot4Behavior pilot) {
    Pilot.DivertInfo di = pilot.getDivertInfo();
    if (di != null && di.getMinutesLeft() <= 0) {
      pilot.processDivert();
      return true;
    } else {
      return false;
    }
  }

  protected boolean processDivertManagement(IPilot4Behavior pilot){
    if (divertIfRequested(pilot) == false){
      adviceDivertTimeIfRequested(pilot);
      return false;
    } else
      return true;
  }
}
