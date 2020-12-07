package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Parentable;
import eng.jAtcSim.newLib.shared.GID;
import eng.jAtcSim.newLib.shared.WithGID;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public abstract class Route extends Parentable<Airport> implements WithGID {

  private final IList<ICommand> routeCommands;
  private final GID gid;

  public Route(IReadOnlyList<ICommand> routeCommands) {
    EAssert.Argument.isNotNull(routeCommands, "routeCommands");
    this.gid = GID.create();
    this.routeCommands = new EList<>(routeCommands);
  }

  @Override
  public GID getGID() {
    return this.gid;
  }

  public IReadOnlyList<ICommand> getRouteCommands() {
    return routeCommands;
  }

  public GID getValue() {
    return gid;
  }
}
