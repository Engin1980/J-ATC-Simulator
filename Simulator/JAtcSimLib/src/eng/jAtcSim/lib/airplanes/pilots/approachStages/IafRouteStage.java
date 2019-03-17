package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.speaking.SpeechList;

public class IafRouteStage extends RouteStage {

  public IafRouteStage(SpeechList route) {
    super(route);
  }

  @Override
  public void initStage(IPilot4Behavior pilot) {
    super.initStage(pilot);
    pilot.setState(Airplane.State.flyingIaf2Faf);
  }
}
