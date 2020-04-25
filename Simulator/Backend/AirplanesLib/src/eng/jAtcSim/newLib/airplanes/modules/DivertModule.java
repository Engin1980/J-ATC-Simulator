package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.airplane2atc.DivertTimeNotification;

public class DivertModule extends Module{
  private final int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};
  private final EDayTimeStamp divertTime;
  private int lastAnnouncedMinute = Integer.MAX_VALUE;
  private boolean possible = true;
  private static final int MINIMAL_DIVERT_TIME_MINUTES = 45;
  private static final int MAXIMAL_DIVERT_TIME_MINUTES = 120;

  public boolean isPossible() {
    return possible;
  }

  public void disable() {
    this.possible = false;
  }

  private static EDayTimeStamp generateDivertTime() {
    EDayTimeStamp now = GAcc.getNow().toStamp();
    int divertTimeMinutes = GAcc.getRnd().nextInt(MINIMAL_DIVERT_TIME_MINUTES, MAXIMAL_DIVERT_TIME_MINUTES);
    EDayTimeStamp ret = now.addMinutes(divertTimeMinutes);
    return ret;
  }

  public DivertModule(IPlaneInterface plane) {
    super(plane);
    this.divertTime = generateDivertTime();
  }

  @Override
  public void elapseSecond(){
    checkForDivert();
  }

  private void checkForDivert() {
    if (possible
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
            plane.getTunedAtc(),
            new DivertTimeNotification(minLeft));
        this.lastAnnouncedMinute = minLeft;
        break;
      }
    }
  }

  private int getMinutesLeft() {
    int diff = (int) Math.ceil((divertTime.getValue() - GAcc.getNow().getValue()) / 60d);
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
