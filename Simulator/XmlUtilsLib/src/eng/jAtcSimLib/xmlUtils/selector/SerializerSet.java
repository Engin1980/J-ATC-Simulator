package eng.jAtcSimLib.xmlUtils.selector;

import eng.eSystem.collections.*;
import eng.eSystem.functionalInterfaces.Predicate;
import eng.jAtcSimLib.xmlUtils.Serializer;

public class SerializerSet {
  private static class Set{
    public final SerializerSelector selector;
    public final Serializer<?> serializer;

    public Set(SerializerSelector selector, Serializer<?> serializer) {
      this.selector = selector;
      this.serializer = serializer;
    }
  }
  private final IList<Set> inner = new EList<>();

  public void add(Class<?> type, Serializer<?> serializer){
    this.add(new TypeSerializerSelector(type,false), serializer);
  }

  public void add(Predicate<Object> condition, Serializer<?> serializer){
    this.add(new ConditionalSerializerSelector(condition), serializer);
  }

  public void add(SerializerSelector serializerSelector, Serializer<?> serializer){
    this.inner.add(new Set(serializerSelector, serializer));
  }
}
