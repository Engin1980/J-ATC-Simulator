package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class ClearedToRouteCommand implements IAtcCommand {

  private Route route;
  private RunwayThreshold expectedRunwayThreshold;

  @XmlConstructor
  private ClearedToRouteCommand() {
  }

  public ClearedToRouteCommand(Route route, RunwayThreshold expectedRunwayThreshold) {

    this.route = route;
    this.expectedRunwayThreshold = expectedRunwayThreshold;
  }

  public Route getRoute() {
    return route;
  }

  public RunwayThreshold getExpectedRunwayThreshold() {
    return expectedRunwayThreshold;
  }

  @Override
  public String toString() {
    return "Clear to route(" + route.getName() + "/" + expectedRunwayThreshold + ") {command}";
  }
}
