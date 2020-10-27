package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.jAtcSimLib.xmlUtils.Serializer;

import java.util.Map;

public class EntriesWithListValuesSerializer<TKey, TItem>
        implements Serializer<Iterable<Map.Entry<TKey, Iterable<TItem>>>> {

  private final Consumer2<XElement, TKey> keyToElementConsumer;
  private final Consumer2<XElement, TItem> valueItemToElementConsumer;

  public EntriesWithListValuesSerializer(Consumer2<XElement, TKey> keyToElementConsumer, Consumer2<XElement, TItem> valueItemToElementConsumer) {
    this.keyToElementConsumer = keyToElementConsumer;
    this.valueItemToElementConsumer = valueItemToElementConsumer;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<Map.Entry<TKey, Iterable<TItem>>> entries) {
    for (Map.Entry<TKey, Iterable<TItem>> entry : entries) {
      XElement entryElement = new XElement(DefaultXmlNames.ENTRY);

      XElement keyElement = new XElement(DefaultXmlNames.MAP_KEY);
      keyToElementConsumer.invoke(keyElement, entry.getKey());
      entryElement.addElement(keyElement);

      XElement valuesElement = new XElement(DefaultXmlNames.VALUES_KEY);
      for (TItem item : entry.getValue()) {
        XElement valueElement = new XElement(DefaultXmlNames.VALUE_KEY);
        valueItemToElementConsumer.invoke(valueElement, item);
        valuesElement.addElement(valueElement);
      }
      entryElement.addElement(valuesElement);
    }
  }
}
