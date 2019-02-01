package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.speaking.SpeechList;

public class RouteStage implements IApproachStage {

  private SpeechList route;

  public RouteStage(SpeechList route) {
    this.route = route;
  }

  @Override
  public void initStage(IPilot4Behavior pilot) {
    pilot.setRoute(route);
    pilot.setState(Airplane.State.flyingIaf2Faf);
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
