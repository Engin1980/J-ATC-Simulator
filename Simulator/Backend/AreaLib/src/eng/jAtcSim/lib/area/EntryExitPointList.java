package eng.jAtcSim.lib.area;

import eng.eSystem.collections.*;

public class EntryExitPointList {
  private final IList<EntryExitPoint> inner = new EList<>();

  public EntryExitPointList(IList<EntryExitPoint> entryExitPoints) {
    for (EntryExitPoint entryExitPoint : entryExitPoints) {
      this.add(entryExitPoint);
    }
  }

  public void add(EntryExitPoint eep) {
    EntryExitPoint eq = inner.tryGetFirst(q -> q.getName().equals(eep.getNavaid().getName()));
    if (eq == null)
      inner.add(eep);
    else
      eq.adjustBy(eep);
  }

  public IReadOnlyList<EntryExitPoint> toReadOnlyList() {
    return inner;
  }
}
