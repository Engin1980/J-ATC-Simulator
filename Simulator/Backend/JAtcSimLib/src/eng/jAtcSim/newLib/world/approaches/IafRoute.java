package eng.jAtcSim.newLib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.speaking.SpeechList;
import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.world.*;
import eng.jAtcSim.newLib.world.xml.XmlLoader;

public class IafRoute extends Route {
  public static IList<IafRoute> loadList(XElement source,
                                        NavaidList navaids, IReadOnlyList<PublishedHold> holds) {
    IList<XElement> daElements = Route.lookForElementRecursively(source, "route");

    IList<IafRoute> ret = new EList<>();
    for (XElement daElement : daElements) {
      IafRoute tmp = IafRoute.load(daElement, navaids, holds);
      ret.add(tmp);
    }
    return ret;
  }

  public static IafRoute load(XElement source, NavaidList navaids, IReadOnlyList<PublishedHold> holds) {
    XmlLoader.setContext(source);

    String iafName = XmlLoader.loadString("iaf");
    PlaneCategoryDefinitions category = XmlLoader.loadPlaneCategory("category", "ABCD");
    String iafMapping = XmlLoader.loadString("iafMapping");

    Navaid iaf = navaids.get(iafName);

    IList<IAtcCommand> cmds = Route.loadCommands(source, navaids, holds);

    IafRoute ret = new IafRoute(iaf, cmds, category, iafMapping);
    return ret;
  }

  public static IafRoute create(Navaid navaid, IList<IAtcCommand> iafRouteCommands) {
    return new IafRoute(navaid, iafRouteCommands, PlaneCategoryDefinitions.getAll(), "");
  }

  private final Navaid navaid;
  private final PlaneCategoryDefinitions category;

  public IafRoute(Navaid navaid, IList<IAtcCommand> routeCommands, PlaneCategoryDefinitions category, String mapping) {
    super(mapping, routeCommands);
    this.navaid = navaid;
    this.category = category;
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return navaid;
  }
}
