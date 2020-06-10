package eng.jAtcSim.newLib.area.airplanes.approachStagePilots;

import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.world.approaches.stages.RouteStage;

public class RouteStagePilot implements IApproachStagePilot<RouteStage> {

  @Override
  public eResult initStage(IAirplaneWriteSimple plane, RouteStage stage) {
    plane.getAdvanced().setRoute(stage.getRoute());
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IAirplaneWriteSimple plane, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IAirplaneWriteSimple plane, RouteStage stage) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IAirplaneWriteSimple plane, RouteStage stage) {
    return plane.getRoutingModule().isRouteEmpty();
  }
}