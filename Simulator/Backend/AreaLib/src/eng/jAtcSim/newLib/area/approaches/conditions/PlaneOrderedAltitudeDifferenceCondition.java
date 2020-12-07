package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class PlaneOrderedAltitudeDifferenceCondition implements ICondition {

  public static PlaneOrderedAltitudeDifferenceCondition create(IntegerPerCategoryValue maximumBelowDifference, IntegerPerCategoryValue maximumAboveDifference) {
    return new PlaneOrderedAltitudeDifferenceCondition(maximumBelowDifference, maximumAboveDifference);
  }

  public static PlaneOrderedAltitudeDifferenceCondition create(Integer maximumBelowDifference, Integer maximumAboveDifference) {
    IntegerPerCategoryValue below = maximumBelowDifference == null ? null : IntegerPerCategoryValue.create(maximumBelowDifference);
    IntegerPerCategoryValue above = maximumAboveDifference == null ? null : IntegerPerCategoryValue.create(maximumAboveDifference);
    return new PlaneOrderedAltitudeDifferenceCondition(below, above);
  }

  private final IntegerPerCategoryValue maximalBelowDifference;
  private final IntegerPerCategoryValue maximalAboveDifference;

  @XmlConstructor
  private PlaneOrderedAltitudeDifferenceCondition() {
    this.maximalBelowDifference = null;
    this.maximalAboveDifference = null;

    PostContracts.register(this, () -> this.maximalAboveDifference != null || this.maximalBelowDifference != null);
  }

  public PlaneOrderedAltitudeDifferenceCondition(IntegerPerCategoryValue maximalBelowDifference, IntegerPerCategoryValue maximalAboveDifference) {

    EAssert.Argument.isTrue(maximalAboveDifference != null || maximalBelowDifference != null);

    this.maximalBelowDifference = maximalBelowDifference;
    this.maximalAboveDifference = maximalAboveDifference;
  }

  @Override
  public String toString() {
    return "PlaneOrderedAltitudeDifferenceCondition{" +
            maximalAboveDifference == null ? "... " : maximalAboveDifference.toString()
            + " -> " +
            maximalBelowDifference == null ? "..." : maximalBelowDifference.toString() +
            '}';
  }

  public IntegerPerCategoryValue tryGetMaximumAboveTargetAltitude() {
    return maximalBelowDifference;
  }

  public IntegerPerCategoryValue tryGetMaximumBelowTargetAltitude() {
    return maximalAboveDifference;
  }
}
