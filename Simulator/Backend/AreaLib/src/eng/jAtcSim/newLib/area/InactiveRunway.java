package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

public class InactiveRunway extends Runway<InactiveRunway, InactiveRunwayThreshold> {

  static class XmlLoader {
    public static InactiveRunway load(XElement source, Airport airport) {
      InactiveRunway ret = new InactiveRunway();
      ret.setParent(airport);
      readThresholds(source, ret);
      return ret;
    }

    protected static void readThresholds(
        XElement source, InactiveRunway activeRunway) {
      IList<InactiveRunwayThreshold> thresholds = InactiveRunwayThreshold.XmlLoader.loadBoth(
          source.getChild("thresholds").getChildren(), activeRunway);
      activeRunway.setThresholds(thresholds);
    }
  }

  private InactiveRunway() {
  }

  @Override
  public String getName() {
    return getThresholdA().getName() + "-" + getThresholdB().getName() + "{inact}";
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwy}";
  }
}
