package eng.newXmlUtils.base;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer3;
import eng.newXmlUtils.XmlContext;

public interface Serializer extends Consumer3<XElement, Object, XmlContext> {
  @Override
  void invoke(XElement e, Object v, XmlContext c);
}
