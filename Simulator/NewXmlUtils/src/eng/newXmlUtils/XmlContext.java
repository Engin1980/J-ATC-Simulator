package eng.newXmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Serializer;
import eng.newXmlUtils.utils.XmlUtils;

public class XmlContext {
  public final SDFManager sdfManager = new SDFManager();
  public final IMap<String, Object> values = new EMap<>();

  public static void serialize(XElement element, Object value, XmlContext context) {
    Serializer serializer = context.sdfManager.getSerializer(value);
    serializer.invoke(element, value, context);
  }

  public static <T> T deserialize(XElement element, XmlContext context, Class<T> type) {
    Deserializer deserializer = context.sdfManager.getDeserializer(type);
    T ret = _deserialize(element, deserializer, context);
    return ret;
  }

  public static Object deserialize(XElement element, XmlContext context) {
    Class<?> type = XmlUtils.loadType(element);
    Deserializer deserializer = context.sdfManager.getDeserializer(type);
    Object ret = _deserialize(element, deserializer, context);
    return ret;
  }

  private static <T> T _deserialize(XElement e, Deserializer d, XmlContext c) {
    T ret = (T) d.invoke(e, c);
    return ret;
  }

}
