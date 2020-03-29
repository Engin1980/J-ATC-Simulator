package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;

public abstract class XmlLoaderWithNavaids<T> implements IXmlLoader<T> {
  public final NavaidList navaids;

  public XmlLoaderWithNavaids(NavaidList navaids) {
    EAssert.Argument.isNotNull(navaids);
    this.navaids = navaids;
  }
}
