package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.world.newApproaches.stages.RouteStage;

public class RouteStagePilot implements IApproachStagePilot<RouteStage> {

  @Override
  public eResult initStage(IPilotWriteSimple pilot, RouteStage stage) {
    pilot.getAdvanced().setRoute(stage.getRoute());
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilotWriteSimple pilot, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilotWriteSimple pilot, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilotWriteSimple pilot, RouteStage stage) {
    return pilot.getRoutingModule().isRouteEmpty();
  }
}
