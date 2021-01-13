package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XIgnored;

public abstract class SimulationModule implements IXPersistable {
  @XIgnored protected final Simulation parent;

  public SimulationModule(Simulation parent) {
    this.parent = parent;
  }

  protected SimulationModule(XContext ctx) {
    this.parent = ctx.loader.parents.get(Simulation.class);
  }
}
