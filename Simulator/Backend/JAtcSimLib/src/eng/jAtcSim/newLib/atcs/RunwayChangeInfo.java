package eng.jAtcSim.newLib.area.atcs;

import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

class RunwayChangeInfo {

  public final ActiveRunwayThreshold newRunwayThreshold;
  public final ETime changeTime;

  public RunwayChangeInfo(ActiveRunwayThreshold newRunwayThreshold, ETime changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}
