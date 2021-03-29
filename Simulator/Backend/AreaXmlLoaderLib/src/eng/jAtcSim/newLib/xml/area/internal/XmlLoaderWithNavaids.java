package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public abstract class XmlLoaderWithNavaids<T> extends XmlLoader<T> {
  public final NavaidList navaids;

  public XmlLoaderWithNavaids(NavaidList navaids, LoadingContext context) {
    super(context);
    EAssert.Argument.isNotNull(navaids);
    this.navaids = navaids;
  }
}
