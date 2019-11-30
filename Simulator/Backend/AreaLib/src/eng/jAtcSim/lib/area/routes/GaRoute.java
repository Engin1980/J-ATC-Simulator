package eng.jAtcSim.lib.area.routes;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.area.Airport;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class GaRoute extends Route {

  public static GaRoute load(XElement source, Airport airport) {
    GaRoute ret = new GaRoute();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  private GaRoute() {
    super();
  }

  private void read(XElement source) {
    GaRoute ret = new GaRoute();
    ret.read(
        source,
        XmlLoader.loadString(source, "gaMapping"));
  }

//  public static GaRoute create(IList<IAtcCommand> gaCommands) {
//    return new GaRoute("", gaCommands);
//  }

//  public GaRoute(String mapping, IList<IAtcCommand> routeCommands) {
//    super(mapping, routeCommands);
//  }
}
