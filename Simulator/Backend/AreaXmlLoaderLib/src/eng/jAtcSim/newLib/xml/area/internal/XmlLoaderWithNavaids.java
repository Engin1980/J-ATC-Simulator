package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;

public abstract class XmlLoaderWithNavaids<T> extends XmlLoader<T> {
  public final NavaidList navaids;

  public XmlLoaderWithNavaids(NavaidList navaids) {
    EAssert.Argument.isNotNull(navaids);
    this.navaids = navaids;
  }
}
