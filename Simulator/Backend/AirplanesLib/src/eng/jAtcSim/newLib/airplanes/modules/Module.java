package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;

public abstract class Module {
  protected final IPlaneInterface plane;

  protected Module(IPlaneInterface plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
  }

  public abstract void elapseSecond();
}
