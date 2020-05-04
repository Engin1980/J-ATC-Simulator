package eng.jAtcSim.newLib.traffic;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;

public class TrafficManagerSettings {
  public final boolean allowDelays;
  public final int maxPlanes;
  public final double densityPercentage;

  public TrafficManagerSettings(boolean allowDelays, int maxPlanes, double densityPercentage) {
    EAssert.Argument.isTrue(maxPlanes >= 0);
    EAssert.Argument.isTrue(NumberUtils.isBetweenOrEqual(0, densityPercentage, 1));
    this.allowDelays = allowDelays;
    this.maxPlanes = maxPlanes;
    this.densityPercentage = densityPercentage;
  }
}
