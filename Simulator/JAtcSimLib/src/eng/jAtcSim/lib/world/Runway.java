package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public abstract class Runway<ThresholdType> extends Parentable<Airport> {
  private final IList<ThresholdType> thresholds;

  public Runway(IList<ThresholdType> thresholds) {
    this.thresholds = thresholds;
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

  @Override
  public String toString() {
    return this.getName() + "{rwy}";
  }
}
