package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IReadOnlyList;

public class InactiveRunway extends Runway<InactiveRunway, InactiveRunwayThreshold> {

  public InactiveRunway(IReadOnlyList<InactiveRunwayThreshold> thresholds) {
    super(thresholds);
  }

  @Override
  public String getName() {
    return getThresholdA().getName() + "-" + getThresholdB().getName() + "{inact}";
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwy}";
  }
}
