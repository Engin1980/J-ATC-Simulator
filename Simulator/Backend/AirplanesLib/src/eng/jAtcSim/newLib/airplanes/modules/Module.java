package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneWriter;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;

public abstract class Module {
  protected final Airplane plane;
  protected final IAirplane rdr;
  protected final IAirplaneWriter wrt;

  protected Module(Airplane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
    this.rdr = plane.getReader();
    this.wrt = plane.getWriter();
  }

  public abstract void elapseSecond();
}
