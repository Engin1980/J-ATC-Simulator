package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

class NavaidXmlLoader implements IXmlLoader<Navaid> {
    public Navaid load(XElement source) {
      XmlLoaderUtils.setContext(source);
      Coordinate coordinate = XmlLoaderUtils.loadCoordinate("coordinate");
      String name = XmlLoaderUtils.loadString("name");
      Navaid.eType type = XmlLoaderUtils.loadEnum("type", Navaid.eType.class);

      Navaid ret = Navaid.create(name, type, coordinate);
      return ret;
    }
}
