package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;

public class PlaneOrderedAltitudeDifference implements ICondition {

  public static PlaneOrderedAltitudeDifference create(IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference) {
    return new PlaneOrderedAltitudeDifference(actualMinusTargetAltitudeMaximalDifference);
  }

  private final IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference;

  public PlaneOrderedAltitudeDifference(IntegerPerCategoryValue actualMinusTargetAltitudeMaximalDifference) {
    EAssert.Argument.isNotNull(actualMinusTargetAltitudeMaximalDifference, "actualMinusTargetAltitudeMaximalDifference");
    this.actualMinusTargetAltitudeMaximalDifference = actualMinusTargetAltitudeMaximalDifference;
  }
}
