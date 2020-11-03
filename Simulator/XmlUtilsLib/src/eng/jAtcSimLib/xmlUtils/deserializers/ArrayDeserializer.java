package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.collections.EList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSimLib.xmlUtils.Deserializer;

import java.lang.reflect.Array;

public class ArrayDeserializer implements Deserializer {

  private final Class<?> arrayComponentType;
  private final Deserializer itemDeserializer;

  public ArrayDeserializer(Class<?> arrayComponentType, Deserializer itemDeserializer) {
    this.itemDeserializer = itemDeserializer;
    this.arrayComponentType = arrayComponentType;
  }

  @Override
  public Object deserialize(XElement element) {
    ItemsDeserializer itemsDeserializer = new ItemsDeserializer(itemDeserializer, new EList<>());

    Iterable<?> iterable = (Iterable<?>) itemsDeserializer.deserialize(element);
    int count = getItemsCountFromIterable(iterable);

    Object ret = Array.newInstance(arrayComponentType, count);
    int index = 0;
    for (Object item : iterable) {
      Array.set(ret, index, item);
      index++;
    }

    return ret;
  }

  private int getItemsCountFromIterable(Iterable<?> iterable) {
    int ret = 0;
    for (Object o : iterable) {
      ret++;
    }
    return ret;
  }
}
