package eng.jAtcSim.newLib.atcs;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;

public interface IAirplane4Atc {
  int getAltitude();

  DARoute getAssignedRoute();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  Callsign getCallsign();

  Coordinate getCoordinate();

  Navaid getDepartureLastNavaid();

  Navaid getEntryExitPoint();

  String getSqwk();

  Airplane.State getState();

  int getTargetAltitude();

  AtcId getTunedAtc();

  AirplaneType getType();

  boolean isArrival();

  boolean isDeparture();

  boolean isEmergency();

  boolean isOnWayToPassDeparturePoint();
}
