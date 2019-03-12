package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.speaking.SpeechList;

public class RouteStage implements IApproachStage {

  private SpeechList route;

  @Override
  public void initStage(IPilot4Behavior pilot) {
    pilot.setRoute(route);
    throw new UnsupportedOperationException("I need to get a route from somewhere!");
  }

  @Override
  public void flyStage(IPilot4Behavior pilot) {
// intentionally blank
  }

  @Override
  public void disposeStage(IPilot4Behavior pilot) {
// intentionally blank
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return pilot.hasEmptyRoute();
  }
}
