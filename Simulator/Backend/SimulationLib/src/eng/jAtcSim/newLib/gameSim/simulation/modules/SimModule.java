package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.jAtcSim.newLib.gameSim.simulation.modules.ISimulationModuleParent;

public abstract class SimModule {
  protected final ISimulationModuleParent parent;

  public SimModule(ISimulationModuleParent parent) {
    this.parent = parent;
  }
}
