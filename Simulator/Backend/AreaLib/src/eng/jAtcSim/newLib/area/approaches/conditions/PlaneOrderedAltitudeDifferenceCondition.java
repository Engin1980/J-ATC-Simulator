package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;

public class PlaneOrderedAltitudeDifferenceCondition implements ICondition {

  public static PlaneOrderedAltitudeDifferenceCondition create(IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference) {
    return new PlaneOrderedAltitudeDifferenceCondition(actualMinusTargetAltitudeMaximalDifference);
  }

  private final IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference;

  public PlaneOrderedAltitudeDifferenceCondition(IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference) {
    EAssert.Argument.isNotNull(actualMinusTargetAltitudeMaximalDifference, "actualMinusTargetAltitudeMaximalDifference");
    EAssert.Argument.isTrue(actualMinusTargetAltitudeMaximalDifference.get('A') <= 0);
    EAssert.Argument.isTrue(actualMinusTargetAltitudeMaximalDifference.get('B') <= 0);
    EAssert.Argument.isTrue(actualMinusTargetAltitudeMaximalDifference.get('C') <= 0);
    EAssert.Argument.isTrue(actualMinusTargetAltitudeMaximalDifference.get('D') <= 0);
    this.actualMinusTargetAltitudeMaximalDifference = actualMinusTargetAltitudeMaximalDifference;
  }

  public IntegerPerCategoryValue getActualMinusTargetAltitudeMaximalDifference() {
    return actualMinusTargetAltitudeMaximalDifference;
  }
}
