package eng.newXmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.functionalInterfaces.Selector;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.implementations.ArrayDeserializer;
import eng.newXmlUtils.implementations.ArraySerializer;

public class XmlFactory {

  public IMap<Class<?>, Deserializer> getSimpleDeserializers() {
    IMap<Class<?>, Selector<String, Object>> forms = new EMap<>();

    forms.set(short.class, q -> Short.valueOf(q));
    forms.set(byte.class, q -> Byte.valueOf(q));
    forms.set(int.class, q -> Integer.valueOf(q));
    forms.set(long.class, q -> Long.valueOf(q));
    forms.set(float.class, q -> Float.valueOf(q));
    forms.set(double.class, q -> Double.valueOf(q));
    forms.set(boolean.class, q -> Boolean.valueOf(q));
    forms.set(char.class, q -> q.charAt(0));
    forms.set(Short.class, q -> Short.valueOf(q));
    forms.set(Byte.class, q -> Byte.valueOf(q));
    forms.set(Integer.class, q -> Integer.valueOf(q));
    forms.set(Long.class, q -> Long.valueOf(q));
    forms.set(Float.class, q -> Float.valueOf(q));
    forms.set(Double.class, q -> Double.valueOf(q));
    forms.set(Boolean.class, q -> Boolean.valueOf(q));
    forms.set(Character.class, q -> q.charAt(0));
    forms.set(String.class, q -> q);

    IMap<Class<?>, Deserializer> ret = new EMap<>();
    for (Class<?> key : forms.getKeys()) {
      ret.set(key, (e, c) -> forms.get(key).invoke(e.getContent()));
    }
    return ret;
  }

  public IMap<Class<?>, Deserializer> getSimpleArrayDeserializers(){
    IMap<Class<?>, Deserializer> ret = new EMap<>();

    ret.set(short[].class, new ArrayDeserializer());
    ret.set(Short[].class, new ArrayDeserializer());
    ret.set(byte[].class, new ArrayDeserializer());
    ret.set(Byte[].class, new ArrayDeserializer());
    ret.set(int[].class, new ArrayDeserializer());
    ret.set(Integer[].class, new ArrayDeserializer());
    ret.set(long[].class, new ArrayDeserializer());
    ret.set(Long[].class, new ArrayDeserializer());
    ret.set(float[].class, new ArrayDeserializer());
    ret.set(Float[].class, new ArrayDeserializer());
    ret.set(double[].class, new ArrayDeserializer());
    ret.set(Double[].class, new ArrayDeserializer());
    ret.set(char[].class, new ArrayDeserializer());
    ret.set(Character[].class, new ArrayDeserializer());
    ret.set(boolean[].class, new ArrayDeserializer());
    ret.set(Boolean[].class, new ArrayDeserializer());
    ret.set(String[].class, new ArrayDeserializer());

    return ret;
  }

  public IMap<Class<?>, Serializer> getSimpleArraySerializers(){
    IMap<Class<?>, Serializer> ret = new EMap<>();

    ret.set(short[].class, new ArraySerializer());
    ret.set(Short[].class, new ArraySerializer());
    ret.set(byte[].class, new ArraySerializer());
    ret.set(Byte[].class, new ArraySerializer());
    ret.set(int[].class, new ArraySerializer());
    ret.set(Integer[].class, new ArraySerializer());
    ret.set(long[].class, new ArraySerializer());
    ret.set(Long[].class, new ArraySerializer());
    ret.set(float[].class, new ArraySerializer());
    ret.set(Float[].class, new ArraySerializer());
    ret.set(double[].class, new ArraySerializer());
    ret.set(Double[].class, new ArraySerializer());
    ret.set(char[].class, new ArraySerializer());
    ret.set(Character[].class, new ArraySerializer());
    ret.set(boolean[].class, new ArraySerializer());
    ret.set(Boolean[].class, new ArraySerializer());
    ret.set(String[].class, new ArraySerializer());

    return ret;
  }

  public IMap<Class<?>, Serializer> getSimpleTypesSerializers() {
    IMap<Class<?>, Selector<Object, String>> parsers = new EMap<>();

    parsers.set(short.class, q -> q.toString());
    parsers.set(byte.class, q -> q.toString());
    parsers.set(int.class, q -> q.toString());
    parsers.set(long.class, q -> q.toString());
    parsers.set(float.class, q -> q.toString());
    parsers.set(double.class, q -> q.toString());
    parsers.set(boolean.class, q -> q.toString());
    parsers.set(char.class, q -> q.toString());
    parsers.set(Short.class, q -> q.toString());
    parsers.set(Byte.class, q -> q.toString());
    parsers.set(Integer.class, q -> q.toString());
    parsers.set(Long.class, q -> q.toString());
    parsers.set(Float.class, q -> q.toString());
    parsers.set(Double.class, q -> q.toString());
    parsers.set(Boolean.class, q -> q.toString());
    parsers.set(Character.class, q -> q.toString());
    parsers.set(String.class, q -> q.toString());

    IMap<Class<?>, Serializer> ret = new EMap<>();
    for (Class<?> key : parsers.getKeys()) {
      ret.set(key, (e, v, c) -> e.setContent(parsers.get(key).invoke(v)));
    }
    return ret;
  }

}
