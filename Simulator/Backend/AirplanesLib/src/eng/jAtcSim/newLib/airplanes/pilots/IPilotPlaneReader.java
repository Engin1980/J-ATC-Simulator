package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Restriction;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IPilotPlaneReader {
  int getAltitude();

  DARoute getAssignedRoute();

  Callsign getCallsign();

  Coordinate getCoordinate();

  int getHeading();

  int getSpeed();

  Restriction getSpeedRestriction();

  Airplane.State getState();

  int getTargetAltitude();

  int getTargetHeading();

  AirplaneType getType();

  /* from .getRoutingModule() */
  boolean hasLateralDirectionAfterCoordinate();

  boolean isArrival();

  boolean isEmergency();

  Coordinate tryGetTargetCoordinate();
}
