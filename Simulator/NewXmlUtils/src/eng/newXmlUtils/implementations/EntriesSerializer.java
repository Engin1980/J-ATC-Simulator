package eng.newXmlUtils.implementations;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.EXmlException;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.utils.InternalXmlUtils;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class EntriesSerializer implements Serializer {
  @Override
  public void invoke(XElement element, Object value, XmlContext xmlContext) {
    InternalXmlUtils.saveType(element, value.getClass());
    Iterable<Map.Entry> entries = getEntries(value);
    for (Map.Entry entry : entries) {
      XElement itemElement = new XElement(InternalXmlUtils.ENTRY);

      storeToElement(itemElement, InternalXmlUtils.KEY, entry.getKey(), xmlContext);
      storeToElement(itemElement, InternalXmlUtils.VALUE, entry.getValue(), xmlContext);

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

  private void storeToElement(XElement parentElement, String elementName, Object object, XmlContext xmlContext) {

    Serializer serializer = xmlContext.sdfManager.getSerializer(object);

    if (serializer != null) {
      XElement itemElement = new XElement(elementName);
      serializer.invoke(itemElement, object, xmlContext);
      InternalXmlUtils.saveType(itemElement, object);
      parentElement.addElement(itemElement);
    }
  }
}
