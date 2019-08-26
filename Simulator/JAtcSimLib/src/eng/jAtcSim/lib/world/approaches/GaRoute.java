package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.xml.XmlLoader;

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
    String gaMapping = XmlLoader.loadString("gaMapping", true);

    IList<IAtcCommand> cmds = Route.loadCommands(source, navaids, holds);

    GaRoute ret = new GaRoute(gaMapping, cmds);
    return ret;
  }

  public GaRoute(String mapping, IList<IAtcCommand> routeCommands) {
    super(mapping, routeCommands);
  }
}
