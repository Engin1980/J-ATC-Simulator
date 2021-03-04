package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import exml.IXPersistable;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;

public abstract class SimulationModule implements IXPersistable {
  @XIgnored
  protected final Simulation parent;

  public SimulationModule(Simulation parent) {
    this.parent = parent;
  }

  protected SimulationModule(XLoadContext ctx) {
    this.parent = ctx.parents.get(Simulation.class);
  }
}
