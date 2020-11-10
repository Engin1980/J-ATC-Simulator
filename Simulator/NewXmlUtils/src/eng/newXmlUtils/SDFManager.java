package eng.newXmlUtils;

import eng.eSystem.collections.IMap;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.InstanceFactory;
import eng.newXmlUtils.base.Serializer;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;

public class SDFManager {

  private Serializer nullSerializer;
  private Deserializer nullDeserializer;
  private IMap<Class, Serializer> serializers;
  private IMap<Class, Deserializer> deserializers;
  private IMap<Class, InstanceFactory<?>> factories;
  private Serializer defaultSerializer = null;
  private Deserializer defaultDeserializer = null;

  public Deserializer getDeserializer(Class<?> type) {
    Deserializer deserializer = coalesce(deserializers.tryGet(type), defaultDeserializer);
    return deserializer;
  }

  public <T> InstanceFactory<T> getFactory(Class<T> type) {
    return (InstanceFactory<T>) factories.get(type);
  }

  public Serializer getSerializer(Object value) {
    return getSerializer(value == null ? null : value.getClass());
  }

  public Serializer getSerializer(Class<?> type) {
    return coalesce(serializers.tryGet(type), defaultSerializer);
  }

  public void register(Class<?> type, Serializer serializer, Deserializer deserializer) {
    if (type == null) {
      nullSerializer = serializer;
      nullDeserializer = deserializer;
    } else {
      this.serializers.set(type, serializer);
      this.deserializers.set(type, deserializer);
    }
  }

  public <T> InstanceFactory<T> tryGetFactory(Class<T> type) {
    return (InstanceFactory<T>) factories.tryGet(type);
  }


}
