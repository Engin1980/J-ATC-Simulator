package eng.jAtcSim.newLib.area.atcs.planeResponsibility;

import eng.jAtcSim.newLib.world.DARoute;
import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;

public class SwitchRoutingRequest {
  public final ActiveRunwayThreshold threshold;
  public final DARoute route;

  public SwitchRoutingRequest(ActiveRunwayThreshold threshold, DARoute route) {
    this.threshold = threshold;
    this.route = route;
  }
}
