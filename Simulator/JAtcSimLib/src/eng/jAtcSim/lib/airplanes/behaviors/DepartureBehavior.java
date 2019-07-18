package eng.jAtcSim.lib.airplanes.behaviors;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;

public class DepartureBehavior extends BasicBehavior {

  @Override
  public void _fly(IAirplaneWriteSimple plane) {
    switch (plane.getState()) {
      case departingLow:
        if (plane.getSha().getAltitude() > 10000)
          plane.setBehaviorAndState(this, Airplane.State.departingHigh);
        break;
      case departingHigh:
        break;
      default:
        super.throwIllegalStateException(plane);
    }
  }

  @Override
  public String toLogString() {
    return "DEP";
  }
}
