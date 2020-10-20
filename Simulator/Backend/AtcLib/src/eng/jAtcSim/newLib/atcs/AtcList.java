package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AtcList<T> extends EDistinctList<T> {

  private final Selector<T, AtcId> atcIdSelector;
  private T lastGot = null;

  AtcList(Selector<T, AtcId> atcIdSelector, Behavior onDuplicateBehavior) {
    super(q -> atcIdSelector.select(q), onDuplicateBehavior);
    this.atcIdSelector = atcIdSelector;
  }

  public T get(AtcId atcId) {
    return this.get(atcId.getName());
  }

  public T get(String atcName) {
    T ret;
    if (lastGot != null && atcIdSelector.select(lastGot).getName().equals(atcName))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> atcIdSelector.select(q).getName().equals(atcName));
      if (ret != null) lastGot = ret;
    }
    return ret;
  }
}
