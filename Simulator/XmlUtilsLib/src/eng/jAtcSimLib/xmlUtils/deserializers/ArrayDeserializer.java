package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.Deserializer;

import java.lang.reflect.Array;

public class ArrayDeserializer implements Deserializer {

  private final Deserializer itemDeserializer;

  public ArrayDeserializer(Deserializer itemDeserializer) {
    this.itemDeserializer = itemDeserializer;
  }

  @Override
  public Object deserialize(XElement element, Class<?> type) {
    EAssert.Argument.isTrue(type.isArray());

    IterableDeserializer iterableDeserializer = new IterableDeserializer(type.getComponentType(), itemDeserializer);

    Iterable<?> iterable = (Iterable<?>) iterableDeserializer.deserialize(element, type);
    int count = getItemsCountFromIterable(iterable);

    Object ret = Array.newInstance(type.getComponentType(), count);
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
