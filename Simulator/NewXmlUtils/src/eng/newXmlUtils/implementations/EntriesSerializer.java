package eng.newXmlUtils.implementations;

import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.utils.InternalXmlUtils;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class EntriesSerializer implements Serializer {
  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    InternalXmlUtils.saveType(element, value.getClass());
    Iterable<Map.Entry> entries = getEntries(value);
    for (Map.Entry entry : entries) {
      XElement itemElement = new XElement(InternalXmlUtils.ITEM);

      XElement keyElement = storeToElement(InternalXmlUtils.KEY, entry.getKey(), xmlContext);
      XElement valueElement = storeToElement(InternalXmlUtils.VALUE, entry.getValue(), xmlContext);

      itemElement.addElement(keyElement);
      itemElement.addElement(valueElement);
      element.addElement(itemElement);
    }
  }

  private Iterable<Map.Entry> getEntries(Object value) {
    Iterable<Map.Entry> ret;
    if (value instanceof IMap)
      ret = ((IMap) value).getEntries();
    else
      throw new EXmlException(sf("Map-Class '%s' is not supported.", value.getClass()));

    return ret;
  }

  private XElement storeToElement(String elementName, Object object, XmlContext xmlContext) {
    XElement ret = new XElement(elementName);

    Serializer serializer = xmlContext.sdfManager.getSerializer(object);

    serializer.invoke(ret, object, xmlContext);
    InternalXmlUtils.saveType(ret, object);

    return ret;
  }
}
