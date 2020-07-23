package eng.jAtcSim.newLib.gameSim.simulation.modules.base;

public abstract class SimModule {
  protected final ISimulationModuleParent parent;

  public SimModule(ISimulationModuleParent parent) {
    this.parent = parent;
  }
}
