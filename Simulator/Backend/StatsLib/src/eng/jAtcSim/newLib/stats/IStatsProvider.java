package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.mood.MoodResult;

public interface IStatsProvider {
  int getElapsedSeconds();

  IReadOnlyList<MoodResult> getFullMoodHistory();

  RecentStats getRecentStats();

  IReadOnlyList<Snapshot> getSnapshots(int step);
}
