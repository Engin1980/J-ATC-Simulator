package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.speeches.ICommand;

public class IafRoute extends Route {

  private final Navaid navaid;
  private final PlaneCategoryDefinitions category;

  public IafRoute(IList<ICommand> routeCommands, Navaid navaid, PlaneCategoryDefinitions category) {
    super(routeCommands);
    EAssert.Argument.isNotNull(navaid, "navaid");
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
