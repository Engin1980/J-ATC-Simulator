package eng.jAtcSim.lib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;

public class InactiveRunway extends Runway<InactiveRunway, InactiveRunwayThreshold> {

  public static InactiveRunway load(XElement source, Airport airport){
    InactiveRunway ret = new InactiveRunway();
    ret.setParent(airport);
    ret.read(source);
    assert ret.getThresholds().size() == 2;
    return ret;
  }

  public void read(XElement source){
    InactiveRunway ret;
    IList<InactiveRunwayThreshold> thresholds = InactiveRunwayThreshold.loadList(source.getChild("thresholds").getChildren());
    assert thresholds.size() == 2;
    ret = new InactiveRunway(thresholds);
    return ret;
  }

  private InactiveRunway(IList<InactiveRunwayThreshold> thresholds) {
    super(thresholds);
    thresholds.forEach(q->q.setParent(this));
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
