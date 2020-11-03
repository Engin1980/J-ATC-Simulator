package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.XmlUtilsException;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ItemsDeserializer implements Deserializer {
  private final Deserializer itemDeserializer;
  private final Object targetInstance;

  public ItemsDeserializer(Deserializer itemDeserializer, Object targetInstance) {
    this.itemDeserializer = itemDeserializer;
    this.targetInstance = targetInstance;
  }

  @Override
  public Object deserialize(XElement element) {
    for (XElement itemElement : element.getChildren(DefaultXmlNames.DEFAULT_ITEM_ELEMENT_NAME)) {
      Object item = itemDeserializer.deserialize(itemElement);
      addToIterable(targetInstance, item);
    }
    return targetInstance;
  }

  private void addToIterable(Object items, Object item) {
    if (items instanceof IList)
      ((IList) items).add(item);
    else if (items instanceof List)
      ((List) items).add(item);
    else if (items instanceof ISet)
      ((ISet) items).add(item);
    else if (items instanceof Set)
      ((Set) items).add((item));
    else if (items instanceof IMap && item instanceof Map.Entry) {
      IMap map = (IMap) items;
      Map.Entry entry = (Map.Entry) item;
      map.set(entry.getKey(), entry.getValue());
    } else if (items instanceof Map && item instanceof Map.Entry) {
      Map map = (Map) items;
      Map.Entry entry = (Map.Entry) item;
      map.put(entry.getKey(), entry.getValue());
    } else
      throw new XmlUtilsException(sf("Unable to add item of type '%s' into iterable of type '%s'.", item == null ? "(null)" : item.getClass(), items.getClass()));
  }
}
