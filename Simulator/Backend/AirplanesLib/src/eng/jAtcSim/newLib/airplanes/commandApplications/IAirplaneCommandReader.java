package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.Restriction;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public interface IAirplaneCommandReader {
  int getAltitude();

  Coordinate getCoordinate();

  int getHeading();

  Restriction getSpeedRestriction();

  Airplane.State getState();

  int getTargetAltitude();

  AirplaneType getType();
}
