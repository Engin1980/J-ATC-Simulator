package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

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
  private GaRoute(IReadOnlyList<IAtcCommand> gaCommands){
    super(gaCommands);
  }

  private void read(XElement source) {
    GaRoute ret = new GaRoute();
    ret.read(
        source,
        XmlLoaderUtils.loadString(source, "gaMapping"));
  }

  public static GaRoute create(IList<IAtcCommand> gaCommands) {
    GaRoute ret = new GaRoute(gaCommands);
    return ret;
  }
}
