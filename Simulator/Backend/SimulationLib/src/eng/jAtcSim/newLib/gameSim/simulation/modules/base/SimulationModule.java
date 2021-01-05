package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.ISimPersistable;
import exml.XContext;

public abstract class SimulationModule implements ISimPersistable {
  protected final Simulation parent;

  public SimulationModule(Simulation parent) {
    this.parent = parent;
  }

  protected SimulationModule(XContext ctx) {
    this.parent = ctx.parent.get(Simulation.class);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.ignoreFields(this, "parent");
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    ctx.saver.ignoreFields(this, "parent");
  }
}
