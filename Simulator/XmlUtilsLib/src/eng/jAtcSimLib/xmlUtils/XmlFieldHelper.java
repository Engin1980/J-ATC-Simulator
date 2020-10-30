package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ReflectionUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ArrayDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.ArraySerializer;

import java.util.Map;

//TODO rename somehow
public class XmlFieldHelper {

  public static IMap<Class<?>, Formatter<?>> defaultFormatters;
  public static IMap<Class<?>, Serializer<?>> defaultSerializers;
  public static IMap<Class<?>, Parser> defaultParsers;
  public static IMap<Class<?>, Deserializer> defaultDeserializers;

  static {
    defaultFormatters = new EMap<>();
    addDefaultFormatter(Integer.class, q -> Integer.toString(q));
    addDefaultFormatter(Short.class, q -> Short.toString(q));
    addDefaultFormatter(Byte.class, q -> Byte.toString(q));
    addDefaultFormatter(Long.class, q -> Long.toString(q));
    addDefaultFormatter(Float.class, q -> Float.toString(q));
    addDefaultFormatter(Double.class, q -> Double.toString(q));
    addDefaultFormatter(Boolean.class, q -> Boolean.toString(q));
    addDefaultFormatter(Character.class, q -> Character.toString(q));
    addDefaultFormatter(String.class, q -> q);

    defaultSerializers = new EMap<>();
    for (Map.Entry<Class<?>, Formatter<?>> entry : defaultFormatters) {
      addDefaultSerializer(entry.getKey(), entry.getValue());
    }

    defaultParsers = new EMap<>();
    defaultParsers.set(Integer.class, q -> Integer.valueOf(q));
    defaultParsers.set(Short.class, q -> Short.valueOf(q));
    defaultParsers.set(Byte.class, q -> Byte.valueOf(q));
    defaultParsers.set(Long.class, q -> Long.valueOf(q));
    defaultParsers.set(Float.class, q -> Float.valueOf(q));
    defaultParsers.set(Double.class, q -> Double.valueOf(q));
    defaultParsers.set(Boolean.class, q -> Boolean.valueOf(q));
    defaultParsers.set(Character.class, q -> q.charAt(0));
    defaultParsers.set(String.class, q -> q);

    defaultDeserializers = new EMap<>();
    for (Map.Entry<Class<?>, Parser> entry : defaultParsers) {
      defaultDeserializers.set(entry.getKey(), entry.getValue().toDeserializer());
    }

  }

  public static Deserializer tryGetDefaultDeserializer(Class<?> type) {
    Deserializer ret = null;
    if (defaultDeserializers.containsKey(type))
      ret = defaultDeserializers.get(type);
    else if (ret == null && type.isEnum())
      ret = (e, q) -> Enum.valueOf((Class<Enum>) type, e.getContent());
    else if (ret == null && type.isArray()) {
      Class<?> arrayItemType = type.getComponentType();
      arrayItemType = ReflectionUtils.ClassUtils.tryWrapPrimitive(arrayItemType);
      Deserializer itemDeserializer = tryGetDefaultDeserializer(arrayItemType);
      if (itemDeserializer != null)
        ret = new ArrayDeserializer(itemDeserializer);
    }
    return ret;
  }

  public static Serializer<?> tryGetDefaultSerializerByValue(Object value) {
    Serializer<?> ret = tryGetDefaultSerializerByClass(value.getClass());
    return ret;
  }

  public static Serializer<?> tryGetDefaultSerializerByClass(Class<?> type) {
    Serializer<?> ret = null;
    if (defaultSerializers.containsKey(type))
      ret = defaultSerializers.get(type);
    if (ret == null && type.isEnum())
      ret = (e, q) -> e.setContent(q.toString());
    if (ret == null && type.isArray()) {
      Class<?> arrayItemType = type.getComponentType();
      arrayItemType = ReflectionUtils.ClassUtils.tryWrapPrimitive(arrayItemType);
      Serializer<?> itemSerializer = tryGetDefaultSerializerByClass(arrayItemType);
      if (itemSerializer != null)
        ret = new ArraySerializer(itemSerializer);
    }
    return ret;
  }

  public static Serializer<?> tryGetSerializer(Object value, IMap<Class<?>, Serializer<?>> customSerializers) {
    Class<?> type = value.getClass();
    if (customSerializers.containsKey(type))
      return customSerializers.get(type);
    else
      return null;
  }

  private static <T> void addDefaultFormatter(Class<T> cls, Formatter<T> formatter) {
    defaultFormatters.set(cls, formatter);
  }

  private static void addDefaultSerializer(Class cls, Formatter formatter) {
    defaultSerializers.set(cls, (XElement e, Object q) -> e.setContent((String) formatter.invoke(q)));
  }

}
