package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.InitialPosition;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class InitialPositionXmlLoader {
  public InitialPosition load(XElement source) {
    XmlLoaderUtils.setContext(source);
    Coordinate coordinate = XmlLoaderUtils.loadCoordinate("coordinate");
    int range = XmlLoaderUtils.loadInteger("range");
    InitialPosition ret = InitialPosition.create(coordinate, range);
    return ret;
  }
}
