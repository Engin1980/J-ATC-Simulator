package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public class HoldingPointBehavior extends Behavior {
  @Override
  public void fly(IPilotWriteSimple pilot) {

  }

  @Override
  public String toLogString() {
    return "{HP}";
  }
}
