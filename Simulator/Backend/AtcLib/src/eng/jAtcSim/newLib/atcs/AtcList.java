package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.shared.AtcId;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class AtcList extends EDistinctList<Atc> implements IXPersistable {
  @XIgnored
  private Atc lastGot = null;

  @XConstructor
  AtcList() {
    super(q -> q.getAtcId().getName(), Behavior.exception);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.setIgnoredFields(this, "selector", "onDuplicateBehavior");
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    ctx.loader.setIgnoredFields(this, "selector", "onDuplicateBehavior");
  }

  Atc get(AtcId atcId) {
    return this.get(atcId.getName());
  }

  Atc get(String atcName) {
    Atc ret;
    if (lastGot != null && lastGot.getAtcId().getName().equals(atcName))
      ret = lastGot;
    else {
      ret = this.tryGetFirst(q -> q.getAtcId().getName().equals(atcName));
      if (ret != null) lastGot = ret;
    }
    return ret;
  }
}
