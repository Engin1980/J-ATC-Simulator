package eng.jAtcSim.lib.area.routes;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.area.Airport;
import eng.jAtcSim.lib.area.Navaid;
import eng.jAtcSim.sharedLib.PlaneCategoryDefinitions;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class IafRoute extends Route {

  public static IafRoute load(XElement source, Airport airport) {
    IafRoute ret = new IafRoute();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  private Navaid navaid;


  //  public static IafRoute create(Navaid navaid, IList<IAtcCommand> iafRouteCommands) {
//    return new IafRoute(navaid, iafRouteCommands, PlaneCategoryDefinitions.getAll(), "");
//  }
  private PlaneCategoryDefinitions category;

  private IafRoute() {
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return navaid;
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
