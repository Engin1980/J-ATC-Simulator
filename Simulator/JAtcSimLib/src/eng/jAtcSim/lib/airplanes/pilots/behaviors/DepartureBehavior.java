package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.jAtcSim.lib.airplanes.Airplane;

public class DepartureBehavior extends BasicBehavior {

  @Override
  public void _fly(IPilot4Behavior pilot) {
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
