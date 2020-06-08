package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.world.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.world.Navaid;
import eng.jAtcSim.newLib.world.DARoute;

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
