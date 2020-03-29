package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;

public abstract class Runway<TParentType, ThresholdType extends Parentable<TParentType>> extends Parentable<Airport> {
  private final IReadOnlyList<ThresholdType> thresholds;

  public Runway(IReadOnlyList<ThresholdType> thresholds) {
    EAssert.Argument.isNotNull(thresholds, "Parameter 'thresholds' cannot be null.");
    EAssert.Argument.isTrue(thresholds.size() == 2);
    this.thresholds = thresholds;
  }

  public ThresholdType get(int index) {
    return thresholds.get(index);
  }

  public abstract String getName();

  public ThresholdType getThresholdA() {
    return thresholds.get(0);
  }

  public ThresholdType getThresholdB() {
    return thresholds.get(1);
  }

  public IReadOnlyList<ThresholdType> getThresholds() {
    return this.thresholds;
  }

  @Override
  public String toString() {
    return this.getName() + "{rwy}";
  }
}
