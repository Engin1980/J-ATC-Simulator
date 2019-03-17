package eng.jAtcSim.lib.world.approaches;

import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Navaid;

public class IafRoute {
  private Navaid navaid;
  private SpeechList<IAtcCommand> routeCommands;
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();

  public IafRoute(Navaid navaid, SpeechList<IAtcCommand> routeCommands, PlaneCategoryDefinitions category) {
    this.navaid = navaid;
    this.routeCommands = routeCommands;
    this.category = category;
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public SpeechList<IAtcCommand> getRouteCommands() {
    return routeCommands;
  }
}
