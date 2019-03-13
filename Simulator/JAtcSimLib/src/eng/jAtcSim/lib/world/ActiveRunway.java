/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IList;

/**
 *
 * @author Marek
 */
public class ActiveRunway extends Runway<ActiveRunwayThreshold> {

  public ActiveRunway(IList<ActiveRunwayThreshold> thresholds, Airport parent) {
    super(thresholds, parent);
  }

  @Override
  public String getName() {
    return getThresholdA().getName() + "-" + getThresholdB().getName();
  }

  @Override
  public String toString() {
    return this.getName() + "{rwy}";
  }
}
