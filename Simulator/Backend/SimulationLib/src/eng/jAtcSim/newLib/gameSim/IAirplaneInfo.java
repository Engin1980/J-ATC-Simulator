package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Atc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneInfo {
  int altitude();

  Callsign callsign();

  Coordinate coordinate();

  Navaid entryExitPoint();

  AirproxType getAirprox();

  DepartureArrival getArriDep();

  DARoute getAssignedRoute();

  ActiveRunwayThreshold getExpectedRunwayThreshold();

  boolean hasRadarContact();

  int heading();

  int ias();

  @Deprecated // used getArriDep() flag instead
  boolean isDeparture();

  boolean isEmergency();

  boolean isMrvaError();

  boolean isUnderConfirmedSwitch();

  AirplaneType planeType();

  AtcId responsibleAtc();

  Squawk squawk();

  String status();

  int targetAltitude();

  int targetHeading();

  int targetSpeed();

  double tas();

  AtcId tunedAtc();

  int verticalSpeed();
}
