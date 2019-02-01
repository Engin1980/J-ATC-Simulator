package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.ApproachInfo;

public abstract class Behavior {

  public abstract void fly(IPilot4Behavior pilot);

  public abstract String toLogString();

  public static void setBehaviorAndState(IPilot4Behavior pilot,
      Behavior behavior, Airplane.State state) {
    pilot.setBehaviorAndState(behavior, state);
  }

  protected void throwIllegalStateException(IPilot4Behavior pilot) {
    throw new ERuntimeException(
        "Illegal state " + pilot.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
