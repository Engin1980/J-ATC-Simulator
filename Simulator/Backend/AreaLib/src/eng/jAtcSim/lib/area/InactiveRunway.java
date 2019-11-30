package eng.jAtcSim.lib.area;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;

public class InactiveRunway extends Runway<InactiveRunway, InactiveRunwayThreshold> {

  public static InactiveRunway load(XElement source, Airport airport) {
    InactiveRunway ret = new InactiveRunway();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  private InactiveRunway() {
  }

  @Override
  public String getName() {
    return getThresholdA().getName() + "-" + getThresholdB().getName() + "{inact}";
  }

  protected void read(XElement source) {
    super.read(source);
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwy}";
  }

  @Override
  protected IList<InactiveRunwayThreshold> readThresholds(XElement source) {
    IList<InactiveRunwayThreshold> ret = InactiveRunwayThreshold.loadBoth(source.getChildren(), this);
    return ret;
  }
}
