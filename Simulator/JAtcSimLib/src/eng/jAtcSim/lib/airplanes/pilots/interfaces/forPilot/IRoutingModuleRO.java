package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Route;

public interface IRoutingModuleRO {
  ActiveRunwayThreshold getAssignedRunwayThreshold();

  boolean hasLateralDirectionAfterCoordinate();

  Route getAssignedRoute();

  boolean hasEmptyRoute();
}
