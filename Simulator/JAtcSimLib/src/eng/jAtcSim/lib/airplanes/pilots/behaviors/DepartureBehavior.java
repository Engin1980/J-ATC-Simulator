package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;

public class DepartureBehavior extends BasicBehavior {

  @Override
  public void _fly(IPilot5Behavior pilot) {
    switch (pilot.getState()) {
      case departingLow:
        if (pilot.getAltitude() > 10000) super.setBehaviorAndState(pilot, this, Airplane.State.departingHigh);
        break;
      case departingHigh:
        break;
      default:
        super.throwIllegalStateException(pilot);
    }
  }

  @Override
  public String toLogString() {
    return "DEP";
  }
}
