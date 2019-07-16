package eng.jAtcSim.lib.airplanes.pilots.modules;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Module;
import eng.jAtcSim.lib.global.ETime;

public class DivertModule extends Module {
  public ETime divertTime;
  public int lastAnnouncedMinute = Integer.MAX_VALUE;

  public DivertModule(IPilot5Module parent) {
    super(parent);
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
