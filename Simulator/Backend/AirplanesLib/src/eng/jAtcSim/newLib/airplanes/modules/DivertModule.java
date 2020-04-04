package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class DivertModule {
  private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};
  private EDayTimeStamp divertTime;
  private int lastAnnouncedMinute = Integer.MAX_VALUE;
  private boolean isPossible = true;

  public void disable() {
    this.isPossible = false;
  }

  public DivertModule(EDayTimeStamp divertTime) {
    this.divertTime = divertTime;
  }
}
