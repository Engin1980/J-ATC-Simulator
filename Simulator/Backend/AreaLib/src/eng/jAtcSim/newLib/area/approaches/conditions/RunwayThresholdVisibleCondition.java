package eng.jAtcSim.newLib.area.approaches.conditions;

public class RunwayThresholdVisibleCondition implements ICondition {
  public static RunwayThresholdVisibleCondition create() {
    return new RunwayThresholdVisibleCondition();
  }

  @Override
  public String toString() {
    return "RunwayThresholdVisibleCondition{}";
  }
}
