package eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;

public interface IRoutingModuleRO {
  Route getAssignedRoute();

  ActiveRunwayThreshold getAssignedRunwayThreshold();

  boolean hasEmptyRoute();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isGoingToFlightOverNavaid(Navaid navaid);
}
