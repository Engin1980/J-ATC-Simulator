package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.jAtcSimLib.xmlUtils.Serializer;

import java.util.Map;

public class EntriesSerializer<TKey, TValue> implements Serializer<Iterable<Map.Entry<TKey, TValue>>> {

  private final Consumer2<XElement, TKey> keyToElementConsumer;
  private final Consumer2<XElement, TValue> valueToElementConsumer;

  public EntriesSerializer(Consumer2<XElement, TKey> keyToElementConsumer,
                           Consumer2<XElement, TValue> valueToElementConsumer) {
    this.keyToElementConsumer = keyToElementConsumer;
    this.valueToElementConsumer = valueToElementConsumer;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<Map.Entry<TKey, TValue>> entries) {
    for (Map.Entry<TKey, TValue> entry : entries) {
      XElement keyElement = new XElement("key");
      keyToElementConsumer.invoke(keyElement, entry.getKey());

      XElement valueElement = new XElement("value");
      valueToElementConsumer.invoke(valueElement, entry.getValue());

      XElement entryElement = new XElement("entry");
      entryElement.addElement(keyElement);
      entryElement.addElement(valueElement);

      targetElement.addElement(entryElement);
    }
  }
}
