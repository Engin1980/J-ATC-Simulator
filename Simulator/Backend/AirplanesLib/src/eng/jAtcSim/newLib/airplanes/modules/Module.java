package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.pilots.IPilotPlane;

public abstract class Module {
  protected final IModulePlane plane;

  protected Module(IModulePlane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
  }

  public abstract void elapseSecond();
}
