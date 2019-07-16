package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;

public abstract class DivertableBehavior extends Behavior {

  @Override
  public boolean isDivertable(){
    return true;
  }

  protected abstract void _fly(IPilot5Behavior pilot);
}
