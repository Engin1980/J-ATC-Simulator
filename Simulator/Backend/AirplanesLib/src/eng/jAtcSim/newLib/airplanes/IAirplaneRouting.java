package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;

public interface IAirplaneRouting {

  String getAssignedDARouteName();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  Navaid getEntryExitPoint();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isDivertable();

  boolean isGoingToFlightOverNavaid(Navaid n);

  default boolean isOnWayToPassDeparturePoint(){
    return isGoingToFlightOverNavaid(getEntryExitPoint());
  }

  boolean isRoutingEmpty();

  Coordinate tryGetTargetCoordinate();

  Coordinate tryGetTargetOrHoldCoordinate();
}
