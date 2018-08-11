package eng.jAtcSim.lib.world;

public class RouteReference {
  public final Route route;
  public final RunwayThreshold threshold;

  public RouteReference(Route route, RunwayThreshold threshold) {
    this.route = route;
    this.threshold = threshold;
  }

  public Route getRoute() {
    return route;
  }

  public RunwayThreshold getThreshold() {
    return threshold;
  }
}
