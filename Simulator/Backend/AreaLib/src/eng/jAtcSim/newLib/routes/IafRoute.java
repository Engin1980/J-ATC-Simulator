package eng.jAtcSim.newLib.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class IafRoute extends Route {

  public static IafRoute create(Airport parent, Navaid navaid, IList<IAtcCommand> iafRouteCommands) {
    IafRoute ret = new IafRoute();
    ret.setParent(parent);
    ret.fill(navaid, iafRouteCommands);
    return ret;
  }

  public static IafRoute load(XElement source, Airport airport) {
    IafRoute ret = new IafRoute();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  private Navaid navaid;
  private PlaneCategoryDefinitions category;

  private IafRoute() {
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  private void fill(Navaid navaid, IList<IAtcCommand> routeCommands) {
    this.navaid = navaid;
    this.category = XmlLoader.loadPlaneCategory("category", "ABCD");
    super.fill(routeCommands);
  }

  private void read(XElement source) {
    XmlLoader.setContext(source);
    String iafName = XmlLoader.loadString("iaf");
    this.navaid = this.getParent().getParent().getNavaids().get(iafName);
    this.category = XmlLoader.loadPlaneCategory("category", "ABCD");
    String iafMapping = XmlLoader.loadString("iafMapping");
    super.read(source, iafMapping);
  }
}
