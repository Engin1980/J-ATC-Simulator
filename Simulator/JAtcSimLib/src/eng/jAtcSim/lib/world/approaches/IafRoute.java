package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.*;
import eng.jAtcSim.lib.world.xml.XmlLoader;

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

    String iafName = XmlLoader.loadString("iaf", true);
    String categoryS = XmlLoader.loadString("category", false);
    PlaneCategoryDefinitions category = categoryS == null ?
        PlaneCategoryDefinitions.getAll() : new PlaneCategoryDefinitions(categoryS);
    String iafMapping = XmlLoader.loadString("iafMapping", true);

    Navaid iaf = navaids.get(iafName);

    IList<IAtcCommand> cmds = Route.loadCommands(source, navaids, holds);

    IafRoute ret = new IafRoute(iaf, cmds, category, iafMapping);
    return ret;
  }

  private Navaid navaid;
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();

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
