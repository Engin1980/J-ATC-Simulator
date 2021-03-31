package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.shared.AtcId;
import exml.IXPersistable;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
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
  public void xSave(XElement elm, XSaveContext ctx) {
    ctx.fields.ignoreFields(this, "selector", "onDuplicateBehavior");
  }

  @Override
  public void xLoad(XElement elm, XLoadContext ctx) {
    ctx.fields.ignoreFields(this, "selector", "onDuplicateBehavior");
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
