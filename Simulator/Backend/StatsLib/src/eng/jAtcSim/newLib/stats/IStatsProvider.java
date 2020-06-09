package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.stats.recent.RecentStats;

public interface IStatsProvider {
  int getElapsedSeconds();

  IReadOnlyList<MoodResult> getFullMoodHistory();

  RecentStats getRecentStats();

  IReadOnlyList<Snapshot> getSnapshots(int step);
}
