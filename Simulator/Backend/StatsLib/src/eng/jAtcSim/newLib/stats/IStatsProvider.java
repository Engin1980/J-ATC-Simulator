package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.stats.recent.RecentStats;

public interface IStatsProvider {
  RecentStats getRecentStats();

  IReadOnlyList<Snapshot> getSnapshots(int step);
}
