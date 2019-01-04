package eng.jAtcSim.lib.atcs.planeResponsibility;

import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class SwitchRoutingRequest {
  public final RunwayThreshold threshold;
  public final Route route;

  public SwitchRoutingRequest(RunwayThreshold threshold, Route route) {
    this.threshold = threshold;
    this.route = route;
  }
}
