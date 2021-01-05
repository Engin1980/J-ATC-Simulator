package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneWriter;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public abstract class Module implements IXPersistable {

  @XIgnored protected final Airplane plane;
  @XIgnored protected final IAirplane rdr;
  @XIgnored protected final IAirplaneWriter wrt;

  protected Module(Airplane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
    this.rdr = plane.getReader();
    this.wrt = plane.getWriter();
  }

  public abstract void elapseSecond();
}
