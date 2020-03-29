package eng.jAtcSim.newLib.area.xml.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.xml.XmlLoaderWithNavaids;
import eng.jAtcSim.newLib.area.xml.XmlMappingDictinary;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;

public class IafRouteXmlLoader extends XmlLoaderWithNavaids<IafRoute> {

  private final XmlMappingDictinary<IafRoute> mappings;

  public IafRouteXmlLoader(NavaidList navaids, XmlMappingDictinary<IafRoute> mappings) {
    super(navaids);
    this.mappings = mappings;
  }

  @Override
  public IafRoute load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String iafName = XmlLoaderUtils.loadString("iaf");
    Navaid navaid = navaids.get(iafName);
    PlaneCategoryDefinitions category = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");
    String mapping = XmlLoaderUtils.loadString("iafMapping");

    IList<ICommand> commands = XmlLoaderUtils.loadList(
        source.getChildren(),
        new eng.jAtcSim.newLib.speeches.xml.XmlLoader()
    );

    IafRoute ret = new IafRoute(commands, navaid, category);
    mappings.add(mapping, ret);
    return ret;
  }
}
