package eng.jAtcSim.newLib.area.xml.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.xml.XmlLoaderWithNavaids;
import eng.jAtcSim.newLib.area.xml.XmlMappingDictinary;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;

public class GaRouteXmlLoader extends XmlLoaderWithNavaids<GaRoute> {
  private final XmlMappingDictinary<GaRoute> mappings;

  public GaRouteXmlLoader(NavaidList navaids, XmlMappingDictinary<GaRoute> mappings) {
    super(navaids);
    this.mappings = mappings;
  }

  @Override
  public GaRoute load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String mapping = XmlLoaderUtils.loadString("gaMapping");

    IList<ICommand> commands = XmlLoaderUtils.loadList(
        source.getChildren(),
        new eng.jAtcSim.newLib.speeches.xml.XmlLoader()
    );

    GaRoute ret = new GaRoute(commands);
    mappings.add(mapping, ret);
    return ret;
  }
}
