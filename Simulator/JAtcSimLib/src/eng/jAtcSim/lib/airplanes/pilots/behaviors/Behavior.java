package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public abstract class Behavior {

  public boolean isDivertable(){
    return false;
  }

  public abstract void fly(IPilotWriteSimple pilot);

  public abstract String toLogString();

  protected void throwIllegalStateException(IPilotWriteSimple pilot) {
    throw new ERuntimeException(
        "Illegal state " + pilot.getPlane().getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
