/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

/**
 * @author Marek
 */
public class ActiveRunway extends Runway<ActiveRunway, ActiveRunwayThreshold> {

  static class XmlReader {
    static ActiveRunway load(XElement source, Airport airport) {
      ActiveRunway ret = new ActiveRunway();
      ret.setParent(airport);
      readThresholds(source, ret);
      return ret;
    }

    protected static void readThresholds(
        XElement source, ActiveRunway activeRunway) {
      IList<ActiveRunwayThreshold> thresholds = ActiveRunwayThreshold.XmlReader.loadBoth(
          source.getChild("thresholds").getChildren(), activeRunway);
      activeRunway.setThresholds(thresholds);
    }
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
