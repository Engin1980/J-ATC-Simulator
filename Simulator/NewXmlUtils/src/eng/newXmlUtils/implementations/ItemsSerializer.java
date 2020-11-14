package eng.newXmlUtils.implementations;

import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.InternalXmlUtils;
import eng.eSystem.eXml.XElement;

public class ItemsSerializer implements Serializer {
  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    InternalXmlUtils.saveType(element, value.getClass());
    Iterable<?> items = (Iterable<?>) value;
    for (Object item : items) {
      XElement itemElement = new XElement(InternalXmlUtils.ITEM);

      Serializer serializer = xmlContext.sdfManager.getSerializer(item);

      if (serializer != null) {
        serializer.invoke(itemElement, item, xmlContext);
        InternalXmlUtils.saveType(itemElement, item);

        element.addElement(itemElement);
      }
    }
  }
}
