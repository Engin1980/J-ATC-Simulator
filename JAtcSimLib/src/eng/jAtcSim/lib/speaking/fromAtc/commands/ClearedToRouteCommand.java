package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Route;

public class ClearedToRouteCommand implements IAtcCommand {

  private Route route;

  public ClearedToRouteCommand(Route route) {
    this.route = route;
  }

  public Route getRoute() {
    return route;
  }

  @Override
  public String toString() {
    return "Clear to route(" + route.getName() + ") {command}";
  }
}
