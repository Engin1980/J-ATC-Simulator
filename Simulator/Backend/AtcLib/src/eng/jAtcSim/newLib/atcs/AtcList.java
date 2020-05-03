package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.newLib.shared.AtcId;

public class AtcList<T> extends EDistinctList<T> {

  private final Selector<T, AtcId> atcIdSelector;
  private T lastGot = null;

  public AtcList(Selector<T, AtcId> atcIdSelector, Behavior onDuplicateBehavior) {
    super(q -> atcIdSelector.getValue(q), onDuplicateBehavior);
    this.atcIdSelector = atcIdSelector;
  }

  public T get(AtcId atcId) {
    return this.get(atcId.getName());
  }

  public T get(String atcName) {
    T ret;
    if (lastGot != null && atcIdSelector.getValue(lastGot).getName().equals(atcName))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> atcIdSelector.getValue(q).getName().equals(atcName));
      if (ret != null) lastGot = ret;
    }
    return ret;
  }
}
