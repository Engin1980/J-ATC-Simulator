package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.stats.StatsProvider;

public class StatsModule {
  private final StatsProvider statsProvider;

  public StatsModule(StatsProvider statsProvider) {
    EAssert.Argument.isNotNull(statsProvider, "statsProvider");

    this.statsProvider = statsProvider;
  }
}
