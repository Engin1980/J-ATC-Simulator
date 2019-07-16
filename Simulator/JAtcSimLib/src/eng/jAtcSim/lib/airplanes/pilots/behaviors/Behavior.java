package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;

public abstract class Behavior {

  public abstract void fly(IPilot5Behavior pilot);

  public abstract String toLogString();

  public static void setBehaviorAndState(IPilot5Behavior pilot,
                                         Behavior behavior, Airplane.State state) {
    pilot.setBehaviorAndState(behavior, state);
  }

  protected void throwIllegalStateException(IPilot5Behavior pilot) {
    throw new ERuntimeException(
        "Illegal state " + pilot.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
