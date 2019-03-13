package eng.jAtcSim.lib.atcs.planeResponsibility;

import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class SwitchRoutingRequest {
  public final ActiveRunwayThreshold threshold;
  public final Route route;

  public SwitchRoutingRequest(ActiveRunwayThreshold threshold, Route route) {
    this.threshold = threshold;
    this.route = route;
  }
}
