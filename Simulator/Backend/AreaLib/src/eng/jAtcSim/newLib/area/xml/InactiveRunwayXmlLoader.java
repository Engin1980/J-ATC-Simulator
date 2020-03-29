package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.InactiveRunway;
import eng.jAtcSim.newLib.area.InactiveRunwayThreshold;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class InactiveRunwayXmlLoader implements IXmlLoader<InactiveRunway> {
  @Override
  public InactiveRunway load(XElement source) {
    IList<InactiveRunwayThreshold> thresholds = new InactiveRunwayThresholdXmlLoader().loadBoth(
        source.getChild("thresholds").getChildren());
    InactiveRunway ret = new InactiveRunway(thresholds);
    return ret;
  }
}
