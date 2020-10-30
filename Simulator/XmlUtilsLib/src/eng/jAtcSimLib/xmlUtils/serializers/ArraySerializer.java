package eng.jAtcSimLib.xmlUtils.serializers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSimLib.xmlUtils.Serializer;

import java.lang.reflect.Array;

public class ArraySerializer implements Serializer<Object> {

  private final Serializer<Iterable<Object>> iterableSerializer;

  public ArraySerializer(Serializer<?> itemSerializer) {
    this.iterableSerializer = new ItemsSerializer<>((Serializer<Object>) itemSerializer);
  }

  @Override
  public void invoke(XElement targetElement, Object value) {
    EAssert.Argument.isTrue(value.getClass().isArray());
    IList<Object> lst = new EList<>();
    for (int i = 0; i < Array.getLength(value); i++) {
      Object item = Array.get(value, i);
      lst.add(item);
    }

    iterableSerializer.invoke(targetElement, lst);
  }
}
