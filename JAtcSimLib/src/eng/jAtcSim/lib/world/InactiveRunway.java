package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public class InactiveRunway {
  private final IList<InactiveRunwayThreshold> thresholds = new EList<>();

  private Airport parent;

  public InactiveRunwayThreshold get(int index){
    return thresholds.get(index);
  }

  public IReadOnlyList<InactiveRunwayThreshold> getThresholds(){
    return this.thresholds;
  }

  public InactiveRunwayThreshold getThresholdA(){
    return thresholds.get(0);
  }

  public InactiveRunwayThreshold getThresholdB(){
    return thresholds.get(1);
  }

  public String getName(){
    return getThresholdA().getName() + "-" + getThresholdB().getName();
  }

  public Airport getParent() {
    return parent;
  }

  public void setParent(Airport parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwy}";
  }
}
