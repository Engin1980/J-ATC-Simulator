package eng.jAtcSim.newLib.airplanes.behaviors;

import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;

public abstract class Behavior {

  public boolean isDivertable(){
    return false;
  }

  public abstract void fly(IAirplaneWriteSimple pilot);

  public abstract String toLogString();

  protected void throwIllegalStateException(IAirplaneWriteSimple pilot) {
    throw new ERuntimeException(
        "Illegal state " + pilot.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
