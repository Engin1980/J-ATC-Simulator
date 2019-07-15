package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.global.ETime;

public class DivertModule {
  private final Pilot.Pilot5Module parent;
  public ETime divertTime;
  public int lastAnnouncedMinute = Integer.MAX_VALUE;

  public DivertModule(Pilot.Pilot5Module parent) {
    this.parent = parent;
  }

  public void init(ETime divertTime){
    this.divertTime = divertTime;
  }

  public int getMinutesLeft() {
    int diff = divertTime.getTotalMinutes() - Acc.now().getTotalMinutes();
    return diff;
  }

  @Deprecated //use getMinutesLeft() instead
  public int getDivertMinutesLeft() {
    return getMinutesLeft();
  }
}
