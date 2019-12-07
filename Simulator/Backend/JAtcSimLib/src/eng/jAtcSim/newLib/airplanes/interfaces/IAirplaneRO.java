package eng.jAtcSim.newLib.airplanes.interfaces;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.Squawk;
import eng.jAtcSim.newLib.airplanes.interfaces.modules.*;
import eng.jAtcSim.newLib.messaging.IMessageParticipant;

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
