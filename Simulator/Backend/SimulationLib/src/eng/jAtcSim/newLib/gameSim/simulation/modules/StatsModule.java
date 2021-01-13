package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.IStatsProvider;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.stats.context.IStatsAcc;
import eng.jAtcSim.newLib.stats.context.StatsAcc;
import exml.XContext;
import exml.annotations.XConstructor;

public class StatsModule extends SimulationModule {
  private final StatsProvider statsProvider;

  public StatsModule(Simulation parent, StatsProvider statsProvider) {
    super(parent);
    EAssert.Argument.isNotNull(statsProvider, "statsProvider");

    this.statsProvider = statsProvider;
  }

  @XConstructor
  public StatsModule(XContext ctx) {
    super(ctx);
    this.statsProvider = null;
  }

  public void elapseSecond() {
    AnalysedPlanes analysedPlanes = parent.getAirplanesModule().getPlanesForStats();
    statsProvider.elapseSecond(analysedPlanes);
  }

  public IStatsProvider getStatsProvider() {
    return statsProvider.getPublicStats();
  }

  public void init() {
    IStatsAcc statsContext = new StatsAcc(this.statsProvider);
    ContextManager.setContext(IStatsAcc.class, statsContext);
  }

  public void registerElapseSecondDuration(int simulationRecalculationLength) {
    statsProvider.registerElapseSecondDuration(simulationRecalculationLength);
  }
}
