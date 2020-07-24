package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

import eng.jAtcSim.newLib.gameSim.simulation.Simulation;

public abstract class SimulationModule {
  protected final Simulation parent;

  public SimulationModule(Simulation parent) {
    this.parent = parent;
  }

}
