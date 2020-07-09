package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

class NavaidXmlLoader implements IXmlLoader<Navaid> {
    public Navaid load(XElement source) {
      log(1, "Xml-loading navaid");
      SmartXmlLoaderUtils.setContext(source);
      String name = SmartXmlLoaderUtils.loadString("name");
      log(2, "... navaid '%s'", name);
      Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");
      Navaid.eType type = SmartXmlLoaderUtils.loadEnum("type", Navaid.eType.class);
      Navaid ret = Navaid.create(name, type, coordinate);
      return ret;
    }
}
