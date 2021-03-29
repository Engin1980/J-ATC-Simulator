//package eng.jAtcSim.newLib.shared.xml;
//
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IMap;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.functionalInterfaces.Selector;
//import eng.eSystem.utilites.ReflectionUtils;
//
//import java.lang.reflect.Field;
//
//import static eng.eSystem.utilites.FunctionShortcuts.sf;
//
//
//public class XmlLoaderUtils {
//
//  public static <T> IList<T> loadList(XElement source, IList<T> target,
//                                      Selector<XElement, Boolean> itemElementSelector,
//                                      Selector<XElement, T> fromElementLoader) {
//    for (XElement child : source.getChildren()) {
//      if (itemElementSelector.invoke(child) == false) continue; // child item element not accepted
//      T item = fromElementLoader.invoke(child);
//      target.add(item);
//    }
//    return target;
//  }
//
//  public static <T> IList<T> loadList(XElement source, IList<T> target,
//                                      Selector<XElement, T> fromElementLoader) {
//    return loadList(source, target, e -> true, fromElementLoader);
//  }
//
//  public static <K, V> IMap<K, V> loadMap(XElement source, IMap<K, V> target,
//                                          Selector<XElement, Boolean> entryElementSelector,
//                                          Selector<XElement, K> keyLoader,
//                                          Selector<XElement, V> valueLoader) {
//
//    for (XElement child : source.getChildren()) {
//      if (entryElementSelector.invoke(child) == false) continue; // child entry element not accepted
//      K key = keyLoader.invoke(child);
//      V value = valueLoader.invoke(child);
//      target.set(key, value);
//    }
//    return target;
//  }
//
//  public static <K, V> IMap<K, V> loadMap(XElement source, IMap<K, V> target,
//                                          Selector<XElement, K> keyLoader,
//                                          Selector<XElement, V> valueLoader) {
//    return loadMap(source, target, e -> true, keyLoader, valueLoader);
//  }
//
//  public static void loadPrimitiveAttribute(XElement elm, Object data, String... fieldNames) {
//    for (String fieldName : fieldNames) {
//      loadPrimitiveAttribute(elm, data, fieldName);
//    }
//  }
//
//  public static void loadPrimitiveAttribute(XElement elm, Object data, String fieldName) {
//    String errMsg = "Unknown error.";
//    try {
//      errMsg = sf("Failed to get class from object '%s'.", data);
//      Class cls = data.getClass();
//      errMsg = sf("Failed to get field '%s' from class '%s'.", fieldName, cls.getName());
//      Field field = ReflectionUtils.ClassUtils.getFields(cls).tryGetFirst(q -> q.getName().equals(fieldName));
//      errMsg = sf("Failed to get attribute value '%s' from xml element '%s'.", fieldName, elm.toXPath());
//      String valueString = elm.getAttribute(fieldName);
//      errMsg = sf("Failed to parse value '%s' into type '%s'.", valueString, field.getType().getName());
//      Object val = parse(valueString, field.getType());
//      field.setAccessible(true);
//      errMsg = sf("Failed to set value '%s' into object of '%s'.", val, data.getClass().getName());
//      field.set(data, val);
//      field.setAccessible(false);
//    } catch (Exception ex) {
//      throw new XmlException(sf("Failed to load field '%s'.'%s' from element '%s'. " + errMsg,
//          data.getClass().getName(), fieldName, elm.toXPath()), ex);
//    }
//  }
//
//  private static Object parse(String valueString, Class<?> type) {
//    Object ret;
//    if (valueString.equals("(null)"))
//      ret = null;
//    else if (type == int.class || type == Integer.class)
//      ret = Integer.valueOf(valueString);
//    else if (type == short.class || type == Short.class)
//      ret = Short.valueOf(valueString);
//    else if (type == byte.class || type == Byte.class)
//      ret = Byte.valueOf(valueString);
//    else if (type == long.class || type == Long.class)
//      ret = Long.valueOf(valueString);
//    else if (type == float.class || type == Float.class)
//      ret = Float.valueOf(valueString);
//    else if (type == char.class || type == Character.class)
//      ret = valueString.charAt(0);
//    else if (type == double.class || type == Double.class)
//      ret = Double.valueOf(valueString);
//    else if (type == boolean.class || type == Boolean.class)
//      ret = Boolean.valueOf(valueString);
//    else if (type.isEnum())
//      ret = Enum.valueOf((Class<Enum>) type, valueString);
//    else
//      throw new RuntimeException("Unknown type to parse " + type.getName());
//    return ret;
//  }
//}
