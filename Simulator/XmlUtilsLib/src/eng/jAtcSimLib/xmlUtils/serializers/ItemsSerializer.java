package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.jAtcSimLib.xmlUtils.Serializer;

public class ItemsSerializer<T> implements Serializer<Iterable<T>> {
  private final Consumer2<XElement, T> itemToElementConsumer;
  private final String itemElementName;

  public ItemsSerializer(Consumer2<XElement, T> itemToElementConsumer) {
    this.itemToElementConsumer = itemToElementConsumer;
    this.itemElementName = DefaultXmlNames.DEFAULT_ITEM_ELEMENT_NAME;
  }

  public ItemsSerializer(Consumer2<XElement, T> itemToElementConsumer, String itemElementName) {
    this.itemToElementConsumer = itemToElementConsumer;
    this.itemElementName = itemElementName;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<T> values) {
    for (T value : values) {
      XElement itemElement = new XElement(itemElementName);
      itemToElementConsumer.invoke(itemElement, value);

      targetElement.addElement(itemElement);
    }
  }
}
