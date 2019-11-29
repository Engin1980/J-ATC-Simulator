package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

class RunwayChangeInfo {

  public final ActiveRunwayThreshold newRunwayThreshold;
  public final ETime changeTime;

  public RunwayChangeInfo(ActiveRunwayThreshold newRunwayThreshold, ETime changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}