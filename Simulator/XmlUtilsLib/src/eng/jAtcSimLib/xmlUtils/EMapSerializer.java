package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;

import java.util.Collection;
import java.util.Map;

public class EMapSerializer<TKey, TValue> implements Serializer<Iterable<Map.Entry<TKey, TValue>>> {

  private final Selector<TKey, String> keyToStringSelector;
  private final Selector<TValue, String> valueToStringSelector;

  public EMapSerializer(Selector<TKey, String> keyToStringSelector, Selector<TValue, String> valueToStringSelector) {
    this.keyToStringSelector = keyToStringSelector;
    this.valueToStringSelector = valueToStringSelector;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<Map.Entry<TKey, TValue>> entries) {
    for (Map.Entry<TKey, TValue> entry : entries) {
      String key =  keyToStringSelector.invoke(entry.getKey());
      XElement keyElement = XmlUtils.saveAsElement("key",key);
      String value = valueToStringSelector.invoke(entry.getValue());
      XElement valueElement = XmlUtils.saveAsElement("value", value);

      XElement entryElement = new XElement("entry");
      entryElement.addElement(keyElement);
      entryElement.addElement(valueElement);

      targetElement.addElement(entryElement);
    }
  }
}
