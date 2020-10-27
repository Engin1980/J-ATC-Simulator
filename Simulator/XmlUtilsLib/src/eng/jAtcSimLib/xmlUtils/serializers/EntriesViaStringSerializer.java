package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

import java.util.Map;

public class EntriesViaStringSerializer<TKey, TValue>  implements Serializer<Iterable<Map.Entry<TKey, TValue>>> {
  private final Selector<TKey, String> keyToStringSelector;
  private final Selector<TValue, String> valueToStringSelector;

  public EntriesViaStringSerializer(Selector<TKey, String> keyToStringSelector, Selector<TValue, String> valueToStringSelector) {
    this.keyToStringSelector = keyToStringSelector;
    this.valueToStringSelector = valueToStringSelector;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<Map.Entry<TKey, TValue>> entries) {
    for (Map.Entry<TKey, TValue> entry : entries) {
      String key =  keyToStringSelector.invoke(entry.getKey());
      XElement keyElement = XmlSaveUtils.saveAsElement("key",key);
      String value = valueToStringSelector.invoke(entry.getValue());
      XElement valueElement = XmlSaveUtils.saveAsElement("value", value);

      XElement entryElement = new XElement("entry");
      entryElement.addElement(keyElement);
      entryElement.addElement(valueElement);

      targetElement.addElement(entryElement);
    }
  }
}
