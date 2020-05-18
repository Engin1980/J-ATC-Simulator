package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

class NavaidXmlLoader implements IXmlLoader<Navaid> {
    public Navaid load(XElement source) {
      SmartXmlLoaderUtils.setContext(source);
      Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");
      String name = SmartXmlLoaderUtils.loadString("name");
      Navaid.eType type = SmartXmlLoaderUtils.loadEnum("type", Navaid.eType.class);

      Navaid ret = Navaid.create(name, type, coordinate);
      return ret;
    }
}
