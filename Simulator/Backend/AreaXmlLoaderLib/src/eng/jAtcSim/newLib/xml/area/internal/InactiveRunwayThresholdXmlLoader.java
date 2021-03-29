package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.InactiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

class InactiveRunwayThresholdXmlLoader {

  IList<InactiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    InactiveRunwayThreshold.Prototype ia = load(sources.get(0));
    InactiveRunwayThreshold.Prototype ib = load(sources.get(1));

    IList<InactiveRunwayThreshold> ret = InactiveRunwayThreshold.create(ia, ib);
    return ret;
  }

  private InactiveRunwayThreshold.Prototype load(XElement source) {
    //TODEL
    //log(3, "Xml-loading inactive threshold");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    //log(3, "... inactive runway threshold '%s'", name);
    Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");

    InactiveRunwayThreshold.Prototype ret = new InactiveRunwayThreshold.Prototype(name, coordinate);
    return ret;
  }
}
