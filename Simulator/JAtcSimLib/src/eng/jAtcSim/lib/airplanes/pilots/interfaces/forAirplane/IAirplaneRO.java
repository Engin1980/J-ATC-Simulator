package eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.global.Restriction;

public interface IAirplaneRO {
  Airplane.State getState() ;

  AirplaneType getType();

  Coordinate getCoordinate();

  IAirplaneFlightRO getFlight();

  IShaRO getSha();

  IEmergencyModuleRO getEmergencyModule();
}
