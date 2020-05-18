package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.InitialPosition;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

public class InitialPositionXmlLoader {
  public InitialPosition load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");
    int range = SmartXmlLoaderUtils.loadInteger("range");
    InitialPosition ret = InitialPosition.create(coordinate, range);
    return ret;
  }
}
