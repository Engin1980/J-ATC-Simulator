package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ICommand;

public class IafRoute extends Route {

  private final Navaid navaid;
  private final PlaneCategoryDefinitions category;

  public static IafRoute create(IList<ICommand> routeCommands, Navaid navaid, PlaneCategoryDefinitions category){
    return new IafRoute(routeCommands, navaid, category);
  }

  private IafRoute(IReadOnlyList<ICommand> routeCommands, Navaid navaid, PlaneCategoryDefinitions category) {
    super(routeCommands);
    EAssert.Argument.isNotNull(navaid, "navaid");
    this.navaid = navaid;
    this.category = category;
  }

  public IafRoute createClone() {
    return new IafRoute(this.getRouteCommands(), navaid, category);
  }

  public PlaneCategoryDefinitions getCategory() {
    return category;
  }

  public Navaid getNavaid() {
    return navaid;
  }
}
