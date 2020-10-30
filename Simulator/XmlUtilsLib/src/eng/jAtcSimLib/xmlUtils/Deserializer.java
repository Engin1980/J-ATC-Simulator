package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;

public interface Deserializer {
  Object deserialize(XElement element);
}
