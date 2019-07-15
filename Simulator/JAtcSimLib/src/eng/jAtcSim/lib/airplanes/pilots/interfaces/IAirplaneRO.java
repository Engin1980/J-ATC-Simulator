package eng.jAtcSim.lib.airplanes.pilots.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;

public interface IAirplaneRO {
  Airplane.State getState() ;

  AirplaneType getType();

  Coordinate getCoordinate();

  IAirplaneFlightRO getFlight();

  IShaRO getSha();

  IEmergencyModuleRO getEmergencyModule();
}
