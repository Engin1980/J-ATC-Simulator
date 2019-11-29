package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.DARoute;

public interface IRoutingModuleRO {
  DARoute getAssignedRoute();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  Navaid getDepartureLastNavaid();

  Navaid getEntryExitPoint();

  default boolean isOnWayToPassDeparturePoint(){
    return this.isGoingToFlightOverNavaid(this.getEntryExitPoint());
  }

  boolean isRouteEmpty();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isGoingToFlightOverNavaid(Navaid navaid);
}
