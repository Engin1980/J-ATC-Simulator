package eng.jAtcSim.newLib.stats.context;

import eng.jAtcSim.newLib.stats.StatsProvider;

public class StatsContext implements IStatsContext {
  private final StatsProvider statsProvider;

  public StatsContext(StatsProvider statsProvider) {
    this.statsProvider = statsProvider;
  }

  @Override
  public StatsProvider getStatsProvider() {
    return this.statsProvider;
  }
}
