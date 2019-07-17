package eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotRO;
import eng.jAtcSim.lib.messaging.IMessageParticipant;

public interface IAirplaneRO extends IMessageParticipant {
  Coordinate getCoordinate();

  IEmergencyModuleRO getEmergencyModule();

  IAirplaneFlightRO getFlight();

  IPilotRO getPilot();

  IShaRO getSha();

  Squawk getSqwk();

  Airplane.State getState();

  AirplaneType getType();
}
