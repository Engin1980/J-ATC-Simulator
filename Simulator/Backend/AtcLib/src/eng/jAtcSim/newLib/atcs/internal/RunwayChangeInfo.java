package eng.jAtcSim.newLib.atcs.internal;

import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

class RunwayChangeInfo {

  public final ActiveRunwayThreshold newRunwayThreshold;
  public final EDayTimeStamp changeTime;

  public RunwayChangeInfo(ActiveRunwayThreshold newRunwayThreshold, EDayTimeStamp changeTime) {
    this.newRunwayThreshold = newRunwayThreshold;
    this.changeTime = changeTime;
  }
}
