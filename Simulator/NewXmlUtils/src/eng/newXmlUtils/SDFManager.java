package eng.newXmlUtils;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Formatter;
import eng.newXmlUtils.base.InstanceFactory;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.implementations.ObjectSerializer;
import eng.newXmlUtils.utils.XmlUtils;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.coalesce;
import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SDFManager {

  private Serializer nullSerializer = (e, v, c) -> e.setContent(XmlUtils.NULL_CONTENT);
  private Deserializer nullDeserializer = (e, v) -> {
    EAssert.isTrue(e.getContent().equals(XmlUtils.NULL_CONTENT), sf("XmlElement '%s' is supposed to have null-value-string content.", e.toFullString()));
    return null;
  };
  private final IMap<Class, Serializer> serializers = new EMap<>();
  private final IMap<Class, Deserializer> deserializers = new EMap<>();
  private final IMap<Class, InstanceFactory<?>> factories = new EMap<>();
  private final ISet<String> autoSerializedPackages = new ESet<>();
  private Serializer defaultSerializer = null;
  private Deserializer defaultDeserializer = null;

  public <T> void setFormatter(Class<T> cls, Formatter<T> formatter) {
    this.serializers.set(cls, formatter.toSerializer());
  }

  public void addAutomaticallySerializedPackage(String packageName) {
    EAssert.Argument.isNonemptyString(packageName);
    this.autoSerializedPackages.add(packageName);
  }

  public Deserializer getDeserializer(Class<?> type) {
    if (type == null)
      return nullDeserializer;

    if (type.isEnum())
      return createEnumDeserializer((Class<Enum>) type);

    Deserializer ret = coalesce(deserializers.tryGet(type), defaultDeserializer);
    if (ret == null)
      throw new EXmlException(sf("Failed to find ret for type '%s'.", type));
    return ret;
  }

  private Deserializer createEnumDeserializer(Class<Enum> type) {
    return ((eng.newXmlUtils.base.Parser) q -> Enum.valueOf(type, q)).toDeserializer();
  }

  public <T> InstanceFactory<T> getFactory(Class<T> type) {
    return (InstanceFactory<T>) factories.get(type);
  }

  public Serializer getSerializer(Object value) {
    return getSerializer(value == null ? null : value.getClass());
  }

  public Serializer getSerializer(Class<?> type) {
    if (type == null)
      return nullSerializer;

    if (type.isEnum())
      return createEnumSerializer((Class<Enum>) type);

    Serializer ret = coalesce(serializers.tryGet(type), defaultSerializer);
    if (ret == null)
      ret = tryGetAutoserializerRegex(type);
    if (ret == null)
      throw new EXmlException(sf("Failed to find serializer for type '%s'.", type));
    return ret;
  }

  private Serializer tryGetAutoserializerRegex(Class<?> type) {
    if (this.autoSerializedPackages.isAny(q -> type.getPackageName().equals(q)))
      return new ObjectSerializer();
    else
      return null;
  }

  private Serializer createEnumSerializer(Class<Enum> type) {
    return ((Formatter<Enum>) q -> q.toString()).toSerializer();
  }

  public void setSerializer(Class<?> type, Serializer serializer) {
    this.serializers.set(type, serializer);
  }

  public void setSerializer(String className, Serializer serializer) {
    Class<?> type;
    try {
      type = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EXmlException(sf("Failed to load type by name '%s'.", className), e);
    }
    this.serializers.set(type, serializer);
  }

  public <T> void setSerializer(Class<? extends T> type, Formatter<T> formatter) {
    this.serializers.set(type, (e, v, c) -> e.setContent(formatter.invoke((T) v)));
  }

  public void setSerializers(IMap<Class<?>, Serializer> serializers) {
    for (Map.Entry<Class<?>, Serializer> entry : serializers) {
      this.setSerializer(entry.getKey(), entry.getValue());
    }
  }

  public void setDeserializers(IMap<Class<?>, Deserializer> deserializers) {
    for (Map.Entry<Class<?>, Deserializer> entry : deserializers) {
      this.setDeserializer(entry.getKey(), entry.getValue());
    }
  }

  private void setDeserializer(Class<?> key, Deserializer deserializer) {
    this.deserializers.set(key, deserializer);
  }

  public <T> InstanceFactory<T> tryGetFactory(Class<T> type) {
    return (InstanceFactory<T>) factories.tryGet(type);
  }
}
