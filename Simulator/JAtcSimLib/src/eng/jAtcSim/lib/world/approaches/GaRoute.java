package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Route;

public class GaRoute extends Route {

  public GaRoute(String mapping, IList<IAtcCommand> routeCommands) {
    super(mapping, routeCommands);
  }
}
