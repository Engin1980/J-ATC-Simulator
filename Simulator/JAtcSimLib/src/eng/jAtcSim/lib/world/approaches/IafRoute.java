package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

public class IafRoute extends Route {
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
