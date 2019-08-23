package eng.jAtcSim.lib.atcs.planeResponsibility;

import eng.jAtcSim.lib.world.DARoute;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class SwitchRoutingRequest {
  public final ActiveRunwayThreshold threshold;
  public final DARoute route;

  public SwitchRoutingRequest(ActiveRunwayThreshold threshold, DARoute route) {
    this.threshold = threshold;
    this.route = route;
  }
}
