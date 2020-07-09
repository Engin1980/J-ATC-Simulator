package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.InactiveRunway;
import eng.jAtcSim.newLib.area.InactiveRunwayThreshold;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;

public class InactiveRunwayXmlLoader implements IXmlLoader<InactiveRunway> {
  @Override
  public InactiveRunway load(XElement source) {
    log(2, "Xml-loading inactive runway");
    IList<InactiveRunwayThreshold> thresholds = new InactiveRunwayThresholdXmlLoader().loadBoth(
        source.getChild("thresholds").getChildren());
    InactiveRunway ret = new InactiveRunway(thresholds);
    return ret;
  }
}
