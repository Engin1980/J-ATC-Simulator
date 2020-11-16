package eng.newXmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.utils.InternalXmlUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlContext {

  public static class XmlContextValues {
    private final IMap<String, Object> inner = new EMap<>();

    public boolean containsKey(String key) {
      return inner.containsKey(key);
    }

    public Object get(String key) {
      EAssert.Argument.isTrue(inner.containsKey(key), sf("Failed to find xml-context value for key '%s'.", key));
      return inner.get(key);
    }

    public <T> T get(Class<T> clz) {
      return (T) this.get(clz.getName());
    }

    public Object getOrSet(String key, Producer<Object> defaultInstanceProducer) {
      if (inner.containsKey(key) == false)
        inner.set(key, defaultInstanceProducer.invoke());
      return get(key);
    }

    public void remove(String key) {
      EAssert.Argument.isTrue(inner.containsKey(key));
      inner.remove(key);
    }

    public void remove(Object object){
      this.remove(object.getClass().getName());
    }

    public <T> void remove(Class<T> clz) {
      this.remove(clz.getName());
    }

    public <T> void set(Class<T> clz, T value) {
      this.set(clz.getName(), value);
    }

    public void set(String key, Object value) {
      EAssert.Argument.isFalse(inner.containsKey(key));
      inner.set(key, value);
    }

    public <T> void set(T value) {
      EAssert.Argument.isNotNull(value);
      this.set((Class<T>) value.getClass(), value);
    }
  }

  public static void serialize(XElement element, Object value, XmlContext context) {
    Serializer serializer = context.sdfManager.getSerializer(value);
    serializer.invoke(element, value, context);
  }

  public static <T> T deserialize(XElement element, XmlContext context, Class<T> type) {
    InternalXmlUtils.saveType(element, type);
    Deserializer deserializer = context.sdfManager.getDeserializer(type);
    T ret = _deserialize(element, deserializer, context);
    return ret;
  }

  public static Object deserialize(XElement element, XmlContext context) {
    Class<?> type = InternalXmlUtils.loadType(element);

    Deserializer deserializer = context.sdfManager.getDeserializer(type);
    Object ret = _deserialize(element, deserializer, context);
    return ret;
  }

  private static <T> T _deserialize(XElement e, Deserializer d, XmlContext c) {
    T ret = (T) d.invoke(e, c);
    return ret;
  }

  public final SDFManager sdfManager = new SDFManager();
  public final XmlContextValues values = new XmlContextValues();

}
