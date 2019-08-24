package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;

public class InactiveRunway extends Runway<InactiveRunway, InactiveRunwayThreshold> {

  public static IList<InactiveRunway> loadList(IReadOnlyList<XElement> sources){
    IList<InactiveRunway> ret = new EList<>();

    for (XElement source : sources) {
      InactiveRunway inactiveRunway = InactiveRunway.load(source);
      ret.add(inactiveRunway);
    }

    return ret;
  }

  public static InactiveRunway load(XElement source){
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
