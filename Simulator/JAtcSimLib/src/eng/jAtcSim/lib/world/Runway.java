package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public abstract class Runway<ThresholdType> {
  private final IList<ThresholdType> thresholds;
  private Airport parent;

  public Runway(IList<ThresholdType> thresholds, Airport parent) {
    this.thresholds = thresholds;
    this.parent = parent;
  }

  public ThresholdType get(int index){
    return thresholds.get(index);
  }

  public IReadOnlyList<ThresholdType> getThresholds(){
    return this.thresholds;
  }

  public ThresholdType getThresholdA(){
    return thresholds.get(0);
  }

  public ThresholdType getThresholdB(){
    return thresholds.get(1);
  }

  public abstract String getName();

  public Airport getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return this.getName() + "{rwy}";
  }
}
