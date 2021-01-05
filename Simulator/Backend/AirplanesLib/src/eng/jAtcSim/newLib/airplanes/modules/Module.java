package eng.jAtcSim.newLib.airplanes.modules;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneWriter;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import exml.ISimPersistable;
import exml.XContext;

public abstract class Module implements ISimPersistable {
  protected final Airplane plane;
  protected final IAirplane rdr;
  protected final IAirplaneWriter wrt;

  protected Module(Airplane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
    this.rdr = plane.getReader();
    this.wrt = plane.getWriter();
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.ignoreFields(this, "rdr", "wrt");
    ctx.saver.ignoreFields(this, "plane");
  }

  @Override
  public void load(XElement elm, XContext ctx) {
  }

  public abstract void elapseSecond();
}
