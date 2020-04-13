package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Restriction;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneCommandReader {
  int getAltitude();

  Callsign getCallsign();

  Coordinate getCoordinate();

  int getHeading();

  int getSpeed();

  Restriction getSpeedRestriction();

  Airplane.State getState();

  int getTargetAltitude();

  AirplaneType getType();

  boolean isArrival();

  boolean isDeparture();

  boolean isEmergency();

  boolean isGoingToFlightOverNavaid(Navaid n);
}
