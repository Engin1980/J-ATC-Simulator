package eng.jAtcSim.newLib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.world.*;
import eng.jAtcSim.newLib.world.xml.XmlLoader;

public class GaRoute extends Route {

  public static IList<GaRoute> loadList(XElement source,
                                        NavaidList navaids, IReadOnlyList<PublishedHold> holds) {
    IList<XElement> daElements = Route.lookForElementRecursively(source, "route");

    IList<GaRoute> ret = new EList<>();
    for (XElement daElement : daElements) {
      GaRoute tmp = GaRoute.load(daElement, navaids, holds);
      ret.add(tmp);
    }
    return ret;
  }

  public static GaRoute load(XElement source, NavaidList navaids, IReadOnlyList<PublishedHold> holds) {
    XmlLoader.setContext(source);
    String gaMapping = XmlLoader.loadString("gaMapping");

    IList<IAtcCommand> cmds = Route.loadCommands(source, navaids, holds);

    GaRoute ret = new GaRoute(gaMapping, cmds);
    return ret;
  }

  public static GaRoute create(IList<IAtcCommand> gaCommands) {
    return new GaRoute("", gaCommands);
  }

  public GaRoute(String mapping, IList<IAtcCommand> routeCommands) {
    super(mapping, routeCommands);
  }
}
