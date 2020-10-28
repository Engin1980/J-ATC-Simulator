package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.jAtcSimLib.xmlUtils.Serializer;

import java.util.Map;

public class EntriesSerializer<TKey, TValue> implements Serializer<Iterable<Map.Entry<TKey, TValue>>> {

  private final Serializer<TKey> keyToElementConsumer;
  private final Serializer<TValue> valueToElementConsumer;

  public EntriesSerializer(Serializer<TKey> keyToElementConsumer,
                           Serializer<TValue> valueToElementConsumer) {
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
