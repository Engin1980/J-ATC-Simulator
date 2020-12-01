package eng.newXmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.functionalInterfaces.Selector2;
import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.base.*;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;
import eng.newXmlUtils.utils.InternalXmlUtils;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SDFManager {

  private final Serializer nullSerializer = (e, v, c) -> e.setContent(InternalXmlUtils.NULL_CONTENT);
  private final Deserializer nullDeserializer = (e, v) -> {
    EAssert.isTrue(e.getContent().equals(InternalXmlUtils.NULL_CONTENT), sf("XmlElement '%s' is supposed to have null-value-string content.", e.toFullString()));
    return null;
  };
  private final IMap<Class, Serializer> serializers = new EMap<>();
  private final IMap<Class, Deserializer> deserializers = new EMap<>();
  private final IMap<Class, InstanceFactory<?>> factories = new EMap<>();
  private Serializer defaultSerializer = null;
  private Deserializer defaultDeserializer = null;
  private final ISet<String> autoPackages = new ESet<>();

  public void addAutoPackage(String packageName) {
    this.autoPackages.add(packageName);
  }

  public Deserializer getDeserializer(Class<?> type) {
    if (type == null)
      return nullDeserializer;

    if (deserializers.containsKey(type))
      return deserializers.get(type);

    if (type.isEnum())
      return createEnumDeserializer((Class<Enum>) type);

    if (isAutoserializedyByPackageName(type))
      return new ObjectDeserializer<>();

    if (defaultDeserializer != null)
      return defaultDeserializer;

    throw new EXmlException(sf("Failed to find deserializer for type '%s'.", type));
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

    if (serializers.containsKey(type))
      return serializers.get(type);

    if (isAutoserializedyByPackageName(type))
      return new ObjectSerializer();

    if (defaultSerializer != null)
      return defaultSerializer;

    throw new EXmlException(sf("Failed to find serializer for type '%s'.", type));
  }

  public void setDeserializer(Class<?> key, Deserializer deserializer) {
    this.deserializers.set(key, deserializer);
  }

  public void setDeserializer(String className, Deserializer deserializer) {
    Class<?> type;
    try {
      type = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EXmlException(sf("Failed to load type by name '%s'.", className), e);
    }
    this.deserializers.set(type, deserializer);
  }

  public void setDeserializers(IMap<Class<?>, Deserializer> deserializers) {
    for (Map.Entry<Class<?>, Deserializer> entry : deserializers) {
      this.setDeserializer(entry.getKey(), entry.getValue());
    }
  }

  public <T> void setFormatter(Class<T> cls, Formatter<T> formatter) {
    this.serializers.set(cls, formatter.toSerializer());
  }

  public <T> void setParser(Class<T> cls, Parser<T> parser) {
    this.deserializers.set(cls, parser.toDeserializer());
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

  public void setSerializers(IMap<Class<?>, Serializer> serializers) {
    for (Map.Entry<Class<?>, Serializer> entry : serializers) {
      this.setSerializer(entry.getKey(), entry.getValue());
    }
  }

  public <T> InstanceFactory<T> tryGetFactory(Class<T> type) {
    return (InstanceFactory<T>) factories.tryGet(type);
  }

  private <T> Deserializer createEnumDeserializer(Class<Enum> type) {
    Parser<T> p = (q, c) -> (T) Enum.valueOf(type, q);
    return p.toDeserializer();
  }

  private boolean isAutoserializedyByPackageName(Class<?> type) {
    return this.autoPackages.isAny(q -> type.getPackageName().equals(q));
  }

  private Serializer createEnumSerializer(Class<Enum> type) {
    return ((Formatter<Enum>) q -> q.toString()).toSerializer();
  }
}
