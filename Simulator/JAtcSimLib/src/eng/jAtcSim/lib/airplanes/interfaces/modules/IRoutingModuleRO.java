package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

public interface IRoutingModuleRO {
  Route getAssignedRoute();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  boolean isRouteEmpty();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isGoingToFlightOverNavaid(Navaid navaid);
}
