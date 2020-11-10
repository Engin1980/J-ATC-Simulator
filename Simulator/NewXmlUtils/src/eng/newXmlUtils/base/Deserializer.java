package eng.newXmlUtils.base;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector2;
import eng.newXmlUtils.XmlContext;

public interface Deserializer extends Selector2<XElement, XmlContext, Object> {

  @Override
  Object invoke(XElement e, XmlContext c);
}
