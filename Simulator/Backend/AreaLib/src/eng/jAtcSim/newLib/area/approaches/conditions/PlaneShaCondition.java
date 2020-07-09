package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;

public class PlaneShaCondition implements ICondition {

  public static PlaneShaCondition create(IntegerPerCategoryValue minAltitude, IntegerPerCategoryValue maxAltitude,
                                         IntegerPerCategoryValue minSpeed, IntegerPerCategoryValue maxSpeed,
                                         IntegerPerCategoryValue minHeading, IntegerPerCategoryValue maxHeading) {
    return new PlaneShaCondition(minAltitude, maxAltitude, minSpeed, maxSpeed, minHeading, maxHeading);
  }

  public static PlaneShaCondition createAsMinimalAltitude(IntegerPerCategoryValue minAltitude) {
    EAssert.Argument.isNotNull(minAltitude, "minAltitude");
    return new PlaneShaCondition(
        minAltitude, null, null, null, null, null);
  }

  public static PlaneShaCondition createAsMaximalAltitude(IntegerPerCategoryValue maxAltitude) {
    EAssert.Argument.isNotNull(maxAltitude, "maxAltitude");
    return new PlaneShaCondition(
        null, maxAltitude, null, null, null, null);
  }

  private final IntegerPerCategoryValue minAltitude;
  private final IntegerPerCategoryValue maxAltitude;
  private final IntegerPerCategoryValue minSpeed;
  private final IntegerPerCategoryValue maxSpeed;
  private final IntegerPerCategoryValue minHeading;
  private final IntegerPerCategoryValue maxHeading;

  private PlaneShaCondition(IntegerPerCategoryValue minAltitude, IntegerPerCategoryValue maxAltitude, IntegerPerCategoryValue minSpeed, IntegerPerCategoryValue maxSpeed, IntegerPerCategoryValue minHeading, IntegerPerCategoryValue maxHeading) {
    EAssert.Argument.isTrue(
        (minHeading != null && maxHeading != null)
        ||
            (maxHeading == null && minHeading == null),
        "Both min/max heading or none must be set or empty."
    );
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.minSpeed = minSpeed;
    this.maxSpeed = maxSpeed;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
  }

  public IntegerPerCategoryValue getMaxAltitude() {
    return maxAltitude;
  }

  public IntegerPerCategoryValue getMaxHeading() {
    return maxHeading;
  }

  public IntegerPerCategoryValue getMaxSpeed() {
    return maxSpeed;
  }

  public IntegerPerCategoryValue getMinAltitude() {
    return minAltitude;
  }

  public IntegerPerCategoryValue getMinHeading() {
    return minHeading;
  }

  public IntegerPerCategoryValue getMinSpeed() {
    return minSpeed;
  }
}
