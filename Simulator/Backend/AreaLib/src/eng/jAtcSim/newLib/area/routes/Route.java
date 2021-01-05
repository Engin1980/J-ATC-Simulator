package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Parentable;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public abstract class Route extends Parentable<Airport> {

  private final IList<ICommand> routeCommands;

  public Route(IReadOnlyList<ICommand> routeCommands) {
    EAssert.Argument.isNotNull(routeCommands, "routeCommands");
    this.routeCommands = new EList<>(routeCommands);
  }

  public IReadOnlyList<ICommand> getRouteCommands() {
    return routeCommands;
  }
}
