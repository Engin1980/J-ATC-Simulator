/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;

/**
 * @author Marek
 */
public class ActiveRunway extends Runway<ActiveRunway, ActiveRunwayThreshold> {

  public ActiveRunway(IReadOnlyList<ActiveRunwayThreshold> thresholds) {
    super(thresholds);
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
