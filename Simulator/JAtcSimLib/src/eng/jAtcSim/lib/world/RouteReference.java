package eng.jAtcSim.lib.world;

public class RouteReference {
  public final Route route;
  public final ActiveRunwayThreshold threshold;

  public RouteReference(Route route, ActiveRunwayThreshold threshold) {
    this.route = route;
    this.threshold = threshold;
  }

  public Route getRoute() {
    return route;
  }

  public ActiveRunwayThreshold getThreshold() {
    return threshold;
  }
}
