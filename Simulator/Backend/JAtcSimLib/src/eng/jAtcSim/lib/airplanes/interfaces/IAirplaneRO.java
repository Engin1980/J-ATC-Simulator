package eng.jAtcSim.lib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.airplanes.interfaces.modules.*;
import eng.jAtcSim.lib.messaging.IMessageParticipant;

public interface IAirplaneRO extends IMessageParticipant {

  IAtcModuleRO getAtcModule();

  IBehaviorModuleRO getBehaviorModule();

  Coordinate getCoordinate();

  IDivertModuleRO getDivertModule();

  IEmergencyModuleRO getEmergencyModule();

  IAirplaneFlightRO getFlightModule();

  IRoutingModuleRO getRoutingModule();

  IMrvaAirproxModule getMrvaAirproxModule();

  IShaRO getSha();

  Squawk getSqwk();

  Airplane.State getState();

  AirplaneType getType();

  Airplane.Airplane4Display getPlane4Display();
}
