/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

/**
 *
 * @author Marek
 */
public class ActiveRunway extends Runway<ActiveRunway, ActiveRunwayThreshold> {


  public static ActiveRunway load(XElement source, Airport airport){
    ActiveRunway ret = new ActiveRunway();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  protected void read(XElement source){
    super.read(source);
  }

  @Override
  protected IList<ActiveRunwayThreshold> readThresholds(XElement source) {
    IList<ActiveRunwayThreshold> ret = ActiveRunwayThreshold.loadBoth(
        source.getChild("thresholds").getChildren(), this);
    return ret;
  }

  private ActiveRunway() {
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
