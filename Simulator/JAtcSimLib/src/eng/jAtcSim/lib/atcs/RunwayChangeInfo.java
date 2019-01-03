package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.world.RunwayThreshold;

class RunwayChangeInfo {

  public final RunwayThreshold newRunwayThreshold;
  public final ETime changeTime;

  public RunwayChangeInfo(RunwayThreshold newRunwayThreshold, ETime changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}