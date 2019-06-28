package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.speaking.SpeechList;

public class RouteStage implements IApproachStage {

  private SpeechList route;

  public RouteStage(SpeechList route) {
    this.route = route;
  }

  @Override
  public eResult initStage(IPilot4Behavior pilot) {
    pilot.setRoute(route);
    throw new UnsupportedOperationException("I need to get a route from somewhere!");
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return pilot.hasEmptyRoute();
  }
}
