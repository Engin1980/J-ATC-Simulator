package eng.jAtcSim.newLib.gameSim.simulation.modules;

public abstract class SimModule {
  protected final ISimulationModuleParent parent;

  public SimModule(ISimulationModuleParent parent) {
    this.parent = parent;
  }
}