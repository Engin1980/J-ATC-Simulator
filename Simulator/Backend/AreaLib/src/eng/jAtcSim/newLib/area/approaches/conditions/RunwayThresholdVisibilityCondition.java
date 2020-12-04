package eng.jAtcSim.newLib.area.approaches.conditions;

public class RunwayThresholdVisibilityCondition implements ICondition {
  public static RunwayThresholdVisibilityCondition create() {
    return new RunwayThresholdVisibilityCondition();
  }

  @Override
  public String toString() {
    return "RunwayThresholdVisibilityCondition{}";
  }
}
