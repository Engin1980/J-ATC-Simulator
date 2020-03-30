package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;

public class PlaneShaCondition implements ICondition {
  private final IntegerPerCategoryValue minAltitude;
  private final IntegerPerCategoryValue maxAltitude;
  private final IntegerPerCategoryValue minSpeed;
  private final IntegerPerCategoryValue maxSpeed;
  private final IntegerPerCategoryValue minHeading;
  private final IntegerPerCategoryValue maxHeading;

  public static PlaneShaCondition createAsMinimalAltitude(IntegerPerCategoryValue minAltitude){
    EAssert.Argument.isNotNull(minAltitude, "minAltitude");
    return new PlaneShaCondition(
        minAltitude, null, null, null, null, null);
  }

  private PlaneShaCondition(IntegerPerCategoryValue minAltitude, IntegerPerCategoryValue maxAltitude, IntegerPerCategoryValue minSpeed, IntegerPerCategoryValue maxSpeed, IntegerPerCategoryValue minHeading, IntegerPerCategoryValue maxHeading) {
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.minSpeed = minSpeed;
    this.maxSpeed = maxSpeed;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
  }

  public IntegerPerCategoryValue getMinAltitude() {
    return minAltitude;
  }

  public IntegerPerCategoryValue getMaxAltitude() {
    return maxAltitude;
  }

  public IntegerPerCategoryValue getMinSpeed() {
    return minSpeed;
  }

  public IntegerPerCategoryValue getMaxSpeed() {
    return maxSpeed;
  }

  public IntegerPerCategoryValue getMinHeading() {
    return minHeading;
  }

  public IntegerPerCategoryValue getMaxHeading() {
    return maxHeading;
  }
}
