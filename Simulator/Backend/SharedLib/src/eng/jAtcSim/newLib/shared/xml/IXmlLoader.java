package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.eXml.XElement;

public interface IXmlLoader<T> extends IXmlLogable {
  T load(XElement source);
}
