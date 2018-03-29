package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.KeyList;

public class InactiveRunway implements KeyItem<String> {
  private final KeyList<InactiveRunwayThreshold, String> thresholds = new KeyList();

  private Airport parent;

  public InactiveRunwayThreshold get(int index){
    return thresholds.get(index);
  }

  public KeyList<InactiveRunwayThreshold,String> getThresholds(){
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

  @Override
  public String getKey() {
    return getName();
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
