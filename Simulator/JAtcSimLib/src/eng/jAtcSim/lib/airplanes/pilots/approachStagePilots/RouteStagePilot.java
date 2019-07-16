package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;
import eng.jAtcSim.lib.world.newApproaches.stages.RouteStage;

public class RouteStagePilot implements IApproachStagePilot<RouteStage> {

  @Override
  public eResult initStage(IPilot5Behavior pilot, RouteStage stage) {
    pilot.setRoute(stage.getRoute());
    throw new UnsupportedOperationException("I need to get a route from somewhere!");
  }

  @Override
  public eResult flyStage(IPilot5Behavior pilot, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilot5Behavior pilot, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot5Behavior pilot, RouteStage stage) {
    return pilot.hasEmptyRoute();
  }
}
