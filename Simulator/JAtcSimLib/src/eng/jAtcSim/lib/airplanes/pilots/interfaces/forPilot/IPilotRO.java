package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;

public interface IPilotRO {
  IAtcModuleRO getAtcModule();

  IDivertModuleRO getDivertModule();

  IAirplaneRO getPlane();

  IRoutingModuleRO getRoutingModule();

  IBehaviorModuleRO getBehaviorModule();
}
