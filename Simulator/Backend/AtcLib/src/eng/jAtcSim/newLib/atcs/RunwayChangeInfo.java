package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.time.ETime;

class RunwayChangeInfo {

  public final ActiveRunwayThreshold newRunwayThreshold;
  public final ETime changeTime;

  public RunwayChangeInfo(ActiveRunwayThreshold newRunwayThreshold, ETime changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}
