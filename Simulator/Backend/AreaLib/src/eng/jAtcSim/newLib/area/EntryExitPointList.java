package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.*;

import java.util.Optional;

public class EntryExitPointList {
  private final IList<EntryExitPoint> inner = new EList<>();

  public EntryExitPointList(IList<EntryExitPoint> entryExitPoints) {
    for (EntryExitPoint entryExitPoint : entryExitPoints) {
      this.add(entryExitPoint);
    }
  }

  public void add(EntryExitPoint eep) {
    Optional<EntryExitPoint> eq = inner.tryGetFirst(q -> q.getName().equals(eep.getNavaid().getName()));
    if (!eq.isPresent())
      inner.add(eep);
    else
      eq.get().adjustBy(eep);
  }

  public IReadOnlyList<EntryExitPoint> toReadOnlyList() {
    return inner;
  }
}
