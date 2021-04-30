package eng.jAtcSim.newLib.stats.context;

import eng.jAtcSim.newLib.newStats.StatsProvider;

public class StatsAcc implements IStatsAcc {
  private final StatsProvider statsProvider;

  public StatsAcc(StatsProvider statsProvider) {
    this.statsProvider = statsProvider;
  }

  @Override
  public StatsProvider getStatsProvider() {
    return this.statsProvider;
  }
}
