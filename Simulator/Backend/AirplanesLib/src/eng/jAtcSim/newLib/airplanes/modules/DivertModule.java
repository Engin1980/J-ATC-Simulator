package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.SharedInstanceProvider;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.airplane2atc.DivertTimeNotification;

public class DivertModule extends Module{
  private final int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};
  private final EDayTimeStamp divertTime;
  private int lastAnnouncedMinute = Integer.MAX_VALUE;
  private boolean isPossible = true;

  public void disable() {
    this.isPossible = false;
  }

  public DivertModule(IModulePlane plane, EDayTimeStamp divertTime) {
    super(plane);
    this.divertTime = divertTime;
  }

  @Override
  public void elapseSecond(){
    checkForDivert();
  }

  private void checkForDivert() {
    if (isPossible
        && plane.isDivertable()
        && plane.getState().is(
        Airplane.State.arrivingHigh, Airplane.State.arrivingLow,
        Airplane.State.holding)
        && plane.isEmergency() == false) {
      boolean isDiverting = this.divertIfRequested();
      if (!isDiverting) {
        adviceDivertTimeIfRequested();
      }
    }
  }

  private void adviceDivertTimeIfRequested() {
    int minLeft = getMinutesLeft();
    for (int dit : divertAnnounceTimes) {
      if (lastAnnouncedMinute > dit && minLeft < dit) {
        plane.sendMessage(
            new DivertTimeNotification(minLeft));
        this.lastAnnouncedMinute = minLeft;
        break;
      }
    }
  }

  private int getMinutesLeft() {
    int diff = divertTime.getTotalMinutes() - SharedInstanceProvider.getNow().getTotalMinutes();
    return diff;
  }

  private boolean divertIfRequested() {
    if (this.getMinutesLeft() <= 0) {
      plane.divert(false);
      return true;
    } else {
      return false;
    }
  }

  public EDayTimeStamp getDivertTime() {
    return divertTime;
  }
}
