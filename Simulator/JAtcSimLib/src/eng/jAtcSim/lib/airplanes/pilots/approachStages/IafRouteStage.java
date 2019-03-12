package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public class IafRouteStage extends RouteStage {

  @Override
  public void initStage(IPilot4Behavior pilot) {
    super.initStage(pilot);
    pilot.setState(Airplane.State.flyingIaf2Faf);
  }
}
