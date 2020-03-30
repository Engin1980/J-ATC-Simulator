package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.InactiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

class InactiveRunwayThresholdXmlLoader  {

  IList<InactiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    InactiveRunwayThreshold.Prototype ia = load(sources.get(0));
    InactiveRunwayThreshold.Prototype ib = load(sources.get(1));

    IList<InactiveRunwayThreshold> ret = InactiveRunwayThreshold.create(ia, ib);
    return ret;
  }

  private InactiveRunwayThreshold.Prototype load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    Coordinate coordinate = XmlLoaderUtils.loadCoordinate("coordinate");

    InactiveRunwayThreshold.Prototype ret = new InactiveRunwayThreshold.Prototype(name, coordinate);
    return ret;
  }
}