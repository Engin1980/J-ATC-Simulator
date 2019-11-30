package eng.jAtcSim.lib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;

public abstract class Runway<TParentType, ThresholdType extends Parentable<TParentType>> extends Parentable<Airport> {
  private IList<ThresholdType> thresholds;

  protected Runway(){}

  protected void read(XElement source){
    IList<ThresholdType> thresholds = readThresholds(source);
    this.thresholds = new EList<>();
    this.thresholds.add(thresholds);
  }

  protected abstract IList<ThresholdType> readThresholds(XElement source);

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
