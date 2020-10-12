package eng.jAtcSim.newLib.speeches.atc.planeSwitching;

import eng.eSystem.validation.EAssert;

public class PlaneSwitchRequestRouting {
  private final String runwayThresholdName;
  private final String routeName;

  public PlaneSwitchRequestRouting(String runwayThresholdName, String routeName) {
    EAssert.isTrue(runwayThresholdName != null || routeName != null);
    this.runwayThresholdName = runwayThresholdName;
    this.routeName = routeName;
  }

  public String getRunwayThresholdName() {
    return runwayThresholdName;
  }

  public String getRouteName() {
    return routeName;
  }
}
