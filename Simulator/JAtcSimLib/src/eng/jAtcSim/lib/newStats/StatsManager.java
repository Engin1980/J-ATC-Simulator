package eng.jAtcSim.lib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public class StatsManager {
  private IList<Collector> collectors = new EList<>();
  private IList<Snapshot> snapshots = new EList<>();

  public StatsManager() {
  }

  public IReadOnlyList<Snapshot> getSnapshots(boolean includeCurrentCollectors) {
    IList<Snapshot> ret;
    if (includeCurrentCollectors)
    {
      ret = new EList<>(snapshots);
      for (Collector collector : collectors) {
        Snapshot s = Snapshot.of(collector);
        ret.add(s);
      }
    } else
      ret = snapshots;

    return ret;
  }
}
