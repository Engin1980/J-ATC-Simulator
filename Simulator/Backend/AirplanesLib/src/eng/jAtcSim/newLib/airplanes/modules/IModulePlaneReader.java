package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.Callsign;

public interface IModulePlaneReader {
  int getAltitude();

  Callsign getCallsign();

  Coordinate getCoordinate();

  int getHeading();

  Airplane.State getState();

  int getTargetAltitude();

  String getTunedAtc();

  AirplaneType getType();

  boolean hasRadarContact();

  boolean isDivertable();

  boolean isEmergency();
}
