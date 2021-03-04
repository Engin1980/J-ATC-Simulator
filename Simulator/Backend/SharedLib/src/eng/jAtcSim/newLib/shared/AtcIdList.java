package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.eXml.XElement;
import exml.IXPersistable;
import exml.loading.XLoadContext;

public class AtcIdList extends EDistinctList<AtcId> implements IXPersistable {

  public AtcIdList() {
    super(q -> q.getName(), Behavior.exception);
  }

  @Override
  public void load(XElement elm, XLoadContext ctx) {
    ctx.ingoreFields(this, "selector", "onDuplicateBehavior");
  }
}
