package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;

import java.util.Map;

//TODO rename somehow
public class XmlFieldHelper {

  public static IMap<Class<?>, Formatter<?>> defaultFormatters;
  public static IMap<Class<?>, Serializer<?>> defaultSerializers;

  static{
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
    for (Map.Entry<Class<?>, Formatter<?>> defaultFormatter : defaultFormatters) {
      addDefaultSerializer2(defaultFormatter.getKey(), defaultFormatter.getValue());
    }
  }

  private static <T> void addDefaultFormatter(Class<T> cls, Formatter<T> formatter){
    defaultFormatters.set(cls, formatter);
  }

  private static <T> void addDefaultSerializer(Class<T> cls, Serializer<T> serializer){
    defaultSerializers.set(cls, serializer);
  }

  private static <T> void addDefaultSerializer(Class<T> cls, Formatter<T> formatter){
    defaultSerializers.set(cls, (XElement e, T q) -> e.setContent(formatter.invoke(q)));
  }

  private static void addDefaultSerializer2(Class cls, Formatter formatter){
    defaultSerializers.set(cls, (XElement e, Object q) -> e.setContent((String)formatter.invoke(q)));
  }

  public static Serializer<?> tryGetDefaultSerializer(Object value){
    Class<?> type = value.getClass();
    if (defaultSerializers.containsKey(type))
    return defaultSerializers.get(type);
    else if (value.getClass().isEnum())
      return (e, q) -> e.setContent(q.toString());
    else
      return null;
  }

  public static Serializer<?> tryGetSerializer(Object value, IMap<Class<?>, Serializer<?>> customSerializers){
    Class<?> type = value.getClass();
    if (customSerializers.containsKey(type))
      return customSerializers.get(type);
    else
      return null;
  }

}
