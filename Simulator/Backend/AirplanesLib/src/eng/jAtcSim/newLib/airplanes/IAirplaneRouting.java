package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;

public interface IAirplaneRouting {
  DARoute getAssignedRoute();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  Navaid getDepartureLastNavaid();

  Navaid getEntryExitPoint();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isDivertable();

  boolean isGoingToFlightOverNavaid(Navaid n);

  default boolean isOnWayToPassDeparturePoint(){
    return isGoingToFlightOverNavaid(getDepartureLastNavaid());
  }

  boolean isRoutingEmpty();

  Coordinate tryGetTargetCoordinate();

  /*
if (targetCoordinate == null
      && parent.getBehaviorModule().is(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class)) {
    eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior hb = parent.getBehaviorModule().getAs(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class);
    targetCoordinate = hb.navaid.getCoordinate();
  }
 */
  Coordinate tryGetTargetOrHoldCoordinate();
}
