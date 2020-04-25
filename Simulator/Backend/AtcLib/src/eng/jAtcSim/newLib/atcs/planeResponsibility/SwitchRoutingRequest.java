package eng.jAtcSim.newLib.atcs.planeResponsibility;

import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;

public class SwitchRoutingRequest {
  public final ActiveRunwayThreshold threshold;
  public final DARoute route;

  public SwitchRoutingRequest(ActiveRunwayThreshold threshold, DARoute route) {
    this.threshold = threshold;
    this.route = route;
  }
}
