package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class ItemsViaStringSerializer<T> implements Serializer<Iterable<T>> {

  private final Selector<T, String> itemToStringSelector;
  private static final String DEFAULT_ITEM_ELEMENT_NAME = "item";
  private final String itemElementName;

  public ItemsViaStringSerializer(Selector<T, String> itemToStringSelector) {
    this.itemToStringSelector = itemToStringSelector;
    this.itemElementName = DEFAULT_ITEM_ELEMENT_NAME;
  }

  public ItemsViaStringSerializer(Selector<T, String> itemToStringSelector, String itemElementName) {
    this.itemToStringSelector = itemToStringSelector;
    this.itemElementName = itemElementName;
  }

  @Override
  public void invoke(XElement targetElement, Iterable<T> values) {
    for (T value : values) {
      String s = itemToStringSelector.invoke(value);
      XElement itemElement = XmlSaveUtils.saveAsElement(itemElementName, s);

      targetElement.addElement(itemElement);
    }
  }
}
