package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.utilites.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlSaverUtils {

  public interface ToElementSaver<TDataType> {
    void save(TDataType data, XElement element);
  }

  public interface FieldCustomSaver {
    void save(Object data, XElement target);
  }

  public static <T> void saveList(IReadOnlyList<T> data, XElement target,
                                  Selector<T, String> itemXmlNameSelector,
                                  ToElementSaver<T> itemToElementSaver) {
    for (T item : data) {
      String itemElementName = itemXmlNameSelector.select(item);
      XElement elm = new XElement(itemElementName);
      itemToElementSaver.save(item, elm);
      target.addElement(elm);
    }
  }

  public static <K, V> void saveMap(IReadOnlyMap<K, V> data, XElement target,
                                    Selector<Map.Entry<K, V>, String> entryXmlNameSelector,
                                    ToElementSaver<K> keyToElementSaver,
                                    ToElementSaver<V> valueToElementSaver) {
    for (Map.Entry<K, V> entry : data.getEntries()) {
      String entryElementName = entryXmlNameSelector.select(entry);
      XElement elm = new XElement(entryElementName);
      keyToElementSaver.save(entry.getKey(), elm);
      valueToElementSaver.save(entry.getValue(), elm);
      target.addElement(elm);
    }
  }

  public static <T> XElement saveObject(T data, String xmlElementName) {
    return saveObject(data, xmlElementName, new EMap<>());
  }

  public static <T> XElement saveObject(T data, String xmlElementName, IMap<String, FieldCustomSaver> fieldCustomSavers) {
    throw new RuntimeException("TODO");
  }

  public static void savePrimitiveAttribute(XElement target, Object source, String... fieldNames) {
    for (String fieldName : fieldNames) {
      savePrimitiveAttribute(target, source, fieldName);
    }
  }

  public static void savePrimitiveAttribute(XElement target, Object source, String fieldName) {
    String errMsg = "Unknown error.";
    try {
      errMsg = sf("Failed to get class from object '%s'.", source);
      Class cls = source.getClass();
      errMsg = sf("Failed to get field '%s' from class '%s'.", fieldName, cls.getName());
      Field field = ReflectionUtils.ClassUtils.getFields(cls).tryGetFirst(q -> q.getName().equals(fieldName));
      errMsg = sf("Failed to read attribute value from '%s'.'%s'.", cls.getName(), fieldName);
      field.setAccessible(true);
      Object value = field.get(source);
      field.setAccessible(false);
      errMsg = sf("Failed to convert attribute value '%s' into string.", value);
      String valueString = format(value);
      errMsg = sf("Failed to write attribute value into the element '%s'.", target.toXPath());
      if (target.hasAttribute(fieldName))
        throw new XmlException(sf("Attribute '%s' already exists in element '%s'.", fieldName, target.toXPath()));
      target.setAttribute(fieldName, valueString);

    } catch (Exception ex) {
      throw new XmlException(sf("Failed to save field '%s'.'%s' into element '%s'. " + errMsg,
          source.getClass().getName(), fieldName, target.toXPath()), ex);
    }
  }

  private static String format(Object value){
    String ret;
    if (value == null)
      ret = "(null)";
    else
      ret = value.toString();
    //TODO delete following if not necessary
//    else if (value.getClass() == Integer.class)
//      ret = value.toString();
//    else if (value.getClass() == Short.class)
//      ret = value.toString();
//    else if (value.getClass() == Byte.class)
//      ret = value.toString();
//    else if (value.getClass() == Long.class)
//      ret = value.toString();
//    else if (value.getClass() == Float.class)
//      ret = value.toString();
//    else if (value.getClass() == Character.class)
//      ret = value.toString();
//    else if (value.getClass() == Double.class)
//      ret = value.toString();
//    else if (value.getClass() == Boolean.class)
//      ret = value.toString();
//    else if (value.getClass().isEnum())
//      ret = value.toString();
//    else
//      throw new RuntimeException("Unknown value.getClass() to parse " + value.getClass().getName());
    return ret;
  }
}
