package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.jAtcSimLib.xmlUtils.serializers.DefaultXmlNames;

import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlSaveUtils {
  public static class Items {
    public static <T> XElement saveAsElement(String elementName, Iterable<T> items,
                                             Serializer<Iterable<T>> serializer) {
      XElement elm = new XElement(elementName);
      saveIntoElementContent(elm, items, serializer);
      return elm;
    }

    public static <T> void saveIntoElementContent(XElement parent, Iterable<T> items,
                                                  Serializer<Iterable<T>> serializer) {
      serializer.invoke(parent, items);
    }

    public static <T> void saveIntoElementChild(XElement parent, String innerElementName,
                                                Iterable<T> items,
                                                Serializer<Iterable<T>> serializer) {
      parent.addElement(
              saveAsElement(innerElementName, items, serializer));
    }
  }

  public static class Entries {
    public static class IListValues {
      public static <K, V> XElement saveAsElement(String elementName, Iterable<Map.Entry<K, IList<V>>> entries,
                                                  Serializer<Iterable<Map.Entry<K, Iterable<V>>>> serializer) {
        XElement elm = new XElement(elementName);
        saveIntoElement(elm, entries, serializer);
        return elm;
      }

      public static <K, V> void saveIntoElement(XElement parent, Iterable<Map.Entry<K, IList<V>>> entries,
                                                Serializer<Iterable<Map.Entry<K, Iterable<V>>>> serializer) {
        Iterable<Map.Entry<K, Iterable<V>>> tmp = convertToIterable(entries);
        serializer.invoke(parent, tmp);
      }

      private static <K, V> Iterable<Map.Entry<K, Iterable<V>>> convertToIterable(Iterable<Map.Entry<K, IList<V>>> entries) {
        IMap<K, Iterable<V>> tmp = new EMap<>();

        for (Map.Entry<K, IList<V>> entry : entries) {
          tmp.set(entry.getKey(), entry.getValue());
        }

        return tmp.getEntries();
      }
    }

    public static class IterableValues {
      public static <K, V> XElement saveAsElement(String elementName, Iterable<Map.Entry<K, Iterable<V>>> entries,
                                                  Serializer<Iterable<Map.Entry<K, Iterable<V>>>> serializer) {
        XElement elm = new XElement(elementName);
        saveIntoElement(elm, entries, serializer);
        return elm;
      }

      public static <K, V> void saveIntoElement(XElement parent, Iterable<Map.Entry<K, Iterable<V>>> entries,
                                                Serializer<Iterable<Map.Entry<K, Iterable<V>>>> serializer) {
        serializer.invoke(parent, entries);
      }
    }

    public static <K, V> XElement saveAsElement(String elementName, Iterable<Map.Entry<K, V>> entries,
                                                Serializer<Iterable<Map.Entry<K, V>>> serializer) {
      XElement elm = new XElement(elementName);
      saveIntoElementContent(elm, entries, serializer);
      return elm;
    }

    public static <K, V> void saveIntoElementContent(XElement parent, Iterable<Map.Entry<K, V>> entries,
                                                     Serializer<Iterable<Map.Entry<K, V>>> serializer) {
      serializer.invoke(parent, entries);
    }

    public static <K, V> void saveIntoElementChild(XElement parent, String innerElementName, Iterable<Map.Entry<K, V>> entries,
                                                   Serializer<Iterable<Map.Entry<K, V>>> serializer) {
      parent.addElement(
              saveAsElement(innerElementName, entries, serializer));
    }
  }

  public static class Field {

    public static void storeField(XElement target, Object object, String fieldName) {
      storeField(target, object, fieldName, null, null);
    }

    public static <T> void storeField(XElement target, Object object, String fieldName, Formatter<T> formatter) {
      Object v = ObjectUtils.getFieldValue(object, fieldName);
      if (v == null)
        saveNullIntoElementChild(target, fieldName);
      else
        saveIntoElementChild(target, fieldName, (T) v, formatter);
    }

    public static <T> void storeField(XElement target, Object object, String fieldName, Serializer<T> serializer) {
      Object v = ObjectUtils.getFieldValue(object, fieldName);

      if (v == null)
        saveNullIntoElementChild(target, fieldName);
      else
        saveIntoElementChild(target, fieldName, (T) v, serializer);
    }

    public static void storeFields(XElement target, Object object, String... fieldNames) {
      storeFields(target, object, fieldNames, null, null);
    }

    public static void storeFields(XElement target, Object object, Iterable<String> fieldNames) {
      storeFields(target, object, new EList<>(fieldNames).toArray(String.class), null, null);
    }

    public static void storeFields(XElement target, Object object, String[] fieldNames,
                                   IMap<java.lang.Class<?>, Serializer<?>> customSerializers) {
      storeFields(target, object, fieldNames, customSerializers, null);
    }

    public static void storeFields(XElement target, Object object, String[] fieldNames,
                                   IMap<java.lang.Class<?>, Serializer<?>> customSerializers,
                                   Serializer<Object> defaultSerializer) {
      for (String fieldName : fieldNames) {
        storeField(target, object, fieldName, customSerializers, defaultSerializer);
      }
    }

    private static void storeField(XElement target, Object object, String fieldName,
                                   IMap<java.lang.Class<?>, Serializer<?>> customSerializers,
                                   Serializer<Object> defaultSerializer) {
      Object v = ObjectUtils.getFieldValue(object, fieldName);

      XElement tmp = new XElement(fieldName);
      target.addElement(tmp);

      if (v == null)
        saveNullIntoElementContent(tmp);
      else {
        Serializer<?> ser = XmlFieldHelper.tryGetDefaultSerializer(v);
        if (ser == null && customSerializers != null && customSerializers.containsKey(v.getClass()))
          ser = customSerializers.get(v.getClass());
        if (ser == null && defaultSerializer != null)
          ser = defaultSerializer;

        if (ser != null)
          saveIntoElementContent(target, v, (Serializer<Object>) ser);
        else
          throw new XmlUtilsException(sf("Failed to save type '%s'. This type is not supported by this function.", v.getClass()));
      }
    }

  }

  public static class Class {
    public static void storeType(XElement target, Object value) {
      saveIntoElementChild(target, DefaultXmlNames.CLASS_NAME, value.getClass().getName());
    }
  }

  private static final String NULL_STRING = "(null)";

  public static XElement saveAsElement(String elementName, long value) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value);
    return ret;
  }

  public static XElement saveAsElement(String elementName, double value) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value);
    return ret;
  }

  public static XElement saveAsElement(String elementName, boolean value) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value);
    return ret;
  }

  public static XElement saveAsElement(String elementName, char value) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value);
    return ret;
  }

  public static XElement saveNullAsElement(String elementName) {
    XElement ret = new XElement(elementName);
    saveNullIntoElementContent(ret);
    return ret;
  }

  public static XElement saveAsElement(String elementName, String value) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value);
    return ret;
  }

  public static <T> XElement saveAsElement(String elementName, T value, Formatter<T> formatter) {
    XElement ret = new XElement(elementName);
    saveIntoElementContent(ret, value, formatter);
    return ret;
  }

  public static <T> XElement saveAsElement(String elementName, T value, Serializer<T> serializer) {
    XElement ret = new XElement(elementName);
    serializer.invoke(ret, value);
    return ret;
  }

  public static void saveNullIntoElementChild(XElement parent, String innerElementName) {
    parent.addElement(saveNullAsElement(innerElementName));
  }

  public static void saveIntoElementChild(XElement parent, String innerElementName, long value) {
    XElement child = saveAsElement(innerElementName, value);
    parent.addElement(child);
  }

  public static void saveIntoElementChild(XElement parent, String innerElementName, double value) {
    XElement child = saveAsElement(innerElementName, value);
    parent.addElement(child);
  }

  public static void saveIntoElementChild(XElement parent, String innerElementName, boolean value) {
    XElement child = saveAsElement(innerElementName, value);
    parent.addElement(child);
  }

  public static void saveIntoElementChild(XElement parent, String innerElementName, char value) {
    XElement child = saveAsElement(innerElementName, value);
    parent.addElement(child);
  }

  public static void saveIntoElementChild(XElement parent, String innerElementName, String value) {
    XElement child = saveAsElement(innerElementName, value);
    parent.addElement(child);
  }

  public static <T> void saveIntoElementChild(XElement parent, String innerElementName, T value, Formatter<T> formatter) {
    XElement child = saveAsElement(innerElementName, value, formatter);
    parent.addElement(child);
  }

  public static <T> void saveIntoElementChild(XElement parent, String innerElementName, T value, Serializer<T> serializer) {
    XElement child = saveAsElement(innerElementName, value, serializer);
    parent.addElement(child);
  }

  public static void saveIntoElementContent(XElement target, long value) {
    target.setContent(Long.toString(value));
  }

  public static void saveIntoElementContent(XElement target, double value) {
    target.setContent(Double.toString(value));
  }

  public static void saveIntoElementContent(XElement target, boolean value) {
    target.setContent(Boolean.toString(value));
  }

  public static void saveIntoElementContent(XElement target, char value) {
    target.setContent(Character.toString(value));
  }

  public static void saveIntoElementContent(XElement target, String value) {
    if (value == null)
      saveNullIntoElementContent(target);
    else
      target.setContent(value);
  }

  public static <T> void saveIntoElementContent(XElement target, T value, Formatter<T> formatter) {
    String s = formatter.invoke(value);
    saveIntoElementContent(target, s);
  }

  public static <T> void saveIntoElementContent(XElement target, T value, Serializer<T> serializer) {
    serializer.invoke(target, value);
  }

  public static void saveIntoElementContent(XElement target, Number value) {
    if (value == null)
      saveNullIntoElementContent(target);
    else
      saveIntoElementContent(target, value.toString());
  }

  public static void saveNullIntoElementContent(XElement target) {
    target.setContent(NULL_STRING);
  }

}
