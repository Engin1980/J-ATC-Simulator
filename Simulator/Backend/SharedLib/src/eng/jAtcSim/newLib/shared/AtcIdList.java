package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.eXml.XElement;
import exml.IXPersistable;
import exml.XContext;

public class AtcIdList extends EDistinctList<AtcId> implements IXPersistable {

  public AtcIdList() {
    super(q -> q.getName(), Behavior.exception);
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    ctx.loader.setIgnoredFields(this, "selector", "onDuplicateBehavior");
  }
}
