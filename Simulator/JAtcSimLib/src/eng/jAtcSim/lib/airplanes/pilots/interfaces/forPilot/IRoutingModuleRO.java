package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.world.Route;

public interface IRoutingModuleRO {
  boolean hasLateralDirectionAfterCoordinate();

  Route getAssignedRoute();
}
