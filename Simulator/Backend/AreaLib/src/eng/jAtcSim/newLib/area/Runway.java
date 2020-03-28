package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public abstract class Runway<TParentType, ThresholdType extends Parentable<TParentType>> extends Parentable<Airport> {
  private IList<ThresholdType> thresholds;

  protected Runway() {
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

  protected void setThresholds(IList<ThresholdType> thresholds) {
    this.thresholds = thresholds;
  }
}
