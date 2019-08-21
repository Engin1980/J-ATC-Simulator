package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;

public class InactiveRunway extends Runway<InactiveRunwayThreshold> {

  public static IList<InactiveRunway> loadList(IReadOnlyList<XElement> sources){

  }

  public static InactiveRunway load(XElement source){
    InactiveRunway ret;



    return ret;
  }

  private InactiveRunway(IList<InactiveRunwayThreshold> thresholds, Airport parent) {
    super(thresholds, parent);
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
