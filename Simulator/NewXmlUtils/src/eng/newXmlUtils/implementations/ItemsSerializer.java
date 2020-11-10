package eng.newXmlUtils.implementations;

import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.XmlUtils;
import eng.eSystem.eXml.XElement;

public class ItemsSerializer implements Serializer {
  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    XmlUtils.saveType(element, value.getClass());
    Iterable<?> items = (Iterable<?>) value;
    for (Object item : items) {
      XElement itemElement = new XElement(XmlUtils.ITEM);

      Serializer serializer = xmlContext.sdfManager.getSerializer(item);

      serializer.invoke(itemElement, item, xmlContext);
      XmlUtils.saveType(itemElement, item);

      element.addElement(itemElement);
    }
  }
}
