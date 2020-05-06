package eng.jAtcSim.newLib.simulation.internal;

public abstract class SimModule {
  protected final ISimulationModuleParent parent;

  public SimModule(ISimulationModuleParent parent) {
    this.parent = parent;
  }
}
