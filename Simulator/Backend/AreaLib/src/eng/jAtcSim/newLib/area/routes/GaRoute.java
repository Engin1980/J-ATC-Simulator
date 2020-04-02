package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.ICommand;

public class GaRoute extends Route {

  public static GaRoute create(IList<ICommand> commands) {
    return new GaRoute(commands);
  }

  public GaRoute(IList<ICommand> routeCommands) {
    super(routeCommands);
  }
}
