package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public interface IXmlLoader<T> extends IXmlLogable {
  T load(XElement source);
}
