package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;

public abstract class Behavior {

  public boolean isDivertable(){
    return false;
  }

  public abstract void fly(IPilot5Behavior pilot);

  public abstract String toLogString();

  protected void throwIllegalStateException(IPilot5Behavior pilot) {
    throw new ERuntimeException(
        "Illegal state " + pilot.getPlane().getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
