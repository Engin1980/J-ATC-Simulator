package eng.newXmlUtils.implementations;

import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Serializer;

public class EnumSerializer implements Serializer {
  @Override
  public void invoke(XElement e, Object v, XmlContext c) {
    e.setContent(v.toString());
  }
}
