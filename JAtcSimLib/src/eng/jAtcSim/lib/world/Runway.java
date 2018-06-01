/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

/**
 *
 * @author Marek
 */
public class Runway {
  private final IList<RunwayThreshold> thresholds = new EList<>();

  private Airport parent;

  public RunwayThreshold get(int index){
    return thresholds.get(index);
  }
  
  public IReadOnlyList<RunwayThreshold> getThresholds(){
    return this.thresholds;
  }
  
  public RunwayThreshold getThresholdA(){
    return thresholds.get(0);
  }
  
  public RunwayThreshold getThresholdB(){
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
    return this.getName() + "{rwy}";
  }
}
