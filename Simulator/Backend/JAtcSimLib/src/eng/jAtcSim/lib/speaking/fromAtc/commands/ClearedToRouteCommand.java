package eng.jAtcSim.lib.speaking.fromAtc.commands;

;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.DARoute;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class ClearedToRouteCommand implements IAtcCommand {

  private DARoute route;
  private ActiveRunwayThreshold expectedRunwayThreshold;

  @XmlConstructor
  private ClearedToRouteCommand() {
  }

  public ClearedToRouteCommand(DARoute route, ActiveRunwayThreshold expectedRunwayThreshold) {

    this.route = route;
    this.expectedRunwayThreshold = expectedRunwayThreshold;
  }

  public DARoute getRoute() {
    return route;
  }

  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return expectedRunwayThreshold;
  }

  @Override
  public String toString() {
    return "Clear to route(" + route.getName() + "/" + expectedRunwayThreshold + ") {command}";
  }
}
