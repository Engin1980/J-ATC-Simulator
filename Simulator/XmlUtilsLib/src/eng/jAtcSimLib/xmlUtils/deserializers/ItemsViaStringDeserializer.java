package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.Parser;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ItemsViaStringDeserializer implements Deserializer {
  private final Deserializer itemDeserializer;
  private final Object items;
  private final Class<?> expectedItemType;

  public ItemsViaStringDeserializer(Parser itemParser, Object items) {
    this(itemParser, items, null);
  }

  public ItemsViaStringDeserializer(Parser itemParser, Object items, Class<?> expectedItemType) {
    this.itemDeserializer = itemParser.toDeserializer();
    this.items = items;
    this.expectedItemType = expectedItemType;
  }

  @Override
  public Object deserialize(XElement element) {
    for (XElement child : element.getChildren(DefaultXmlNames.DEFAULT_ITEM_ELEMENT_NAME)) {
      Object item = itemDeserializer.deserialize(child);

      if (expectedItemType != null && expectedItemType.isAssignableFrom(item.getClass()) == false)
        throw new XmlUtilsException(sf("Item added to the items-object should be of type '%s', but is of type '%s'.",
                this.expectedItemType, item.getClass()));

      addItemToItems(item);
    }
    return items;
  }

  private void addItemToItems(Object item) {
    if (items instanceof IList)
      ((IList) items).add(item);
    else if (items instanceof ISet)
      ((ISet) items).add(item);
    else
      throw new UnsupportedOperationException("Unspported type to load - " + items.getClass().getName());
  }
}
