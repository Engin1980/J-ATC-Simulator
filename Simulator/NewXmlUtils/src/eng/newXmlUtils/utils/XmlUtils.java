package eng.newXmlUtils.utils;

import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.EXmlException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlUtils {
  public static final String KEY = "key";
  public static final String VALUE = "value";
  public static final String ITEM = "item";
  public static final String ENTRY = "entry";
  public static final String TYPE_NAME = "__type";
  public static final String NULL_CONTENT = "(null)";
  private static final String COMPONENT_TYPE_NAME = "__item_type";

  public static Class<?> loadType(XElement element) {
    String s = element.getAttribute(TYPE_NAME);
    if (s.equals(NULL_CONTENT))
      return null;
    else {
      Class<?> cls = loadClassFromString(s);
      return cls;
    }
  }

  public static void saveType(XElement element, Class<?> type) {
    element.setAttribute(TYPE_NAME, type.getName());
  }

  public static void saveType(XElement element, Object value) {
    if (value == null)
      element.setAttribute(TYPE_NAME, NULL_CONTENT);
    else
      saveType(element, value.getClass());
  }

  public static Class<?> loadComponentType(XElement element) {
    String s = element.getAttribute(COMPONENT_TYPE_NAME);
    Class<?> ret = loadClassFromString(s);
    return ret;
  }

  public static void saveComponentType(XElement element, Class<?> componentType) {
    element.setAttribute(COMPONENT_TYPE_NAME, componentType.getName());
  }

  private static Class<?> loadClassFromString(String className) {
    Class ret;
    try {
      ret = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new EXmlException(sf("Failed to load type '%s'"), e);
    }
    return ret;
  }
}
