package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSimLib.xmlUtils.formatters.CoordinateFormatter;

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
      saveIntoElement(elm, entries, serializer);
      return elm;
    }

    public static <K, V> void saveIntoElement(XElement parent, Iterable<Map.Entry<K, V>> entries,
                                              Serializer<Iterable<Map.Entry<K, V>>> serializer) {
      serializer.invoke(parent, entries);
    }

  }

  public static class Field {

    public static void storeField(XElement target, Object object, String fieldName) {
      Object v = ObjectUtils.getFieldValue(object, fieldName);

      XElement tmp = new XElement(fieldName);
      target.addElement(tmp);

      if (v == null)
        XmlSaveUtils.saveNullIntoElementContent(tmp);
      else if (v instanceof Short)
        XmlSaveUtils.saveIntoElementContent(tmp, (short) v);
      else if (v instanceof Byte)
        XmlSaveUtils.saveIntoElementContent(tmp, (byte) v);
      else if (v instanceof Integer)
        XmlSaveUtils.saveIntoElementContent(tmp, (int) v);
      else if (v instanceof Long)
        XmlSaveUtils.saveIntoElementContent(tmp, (long) v);
      else if (v instanceof Float)
        XmlSaveUtils.saveIntoElementContent(tmp, (float) v);
      else if (v instanceof Double)
        XmlSaveUtils.saveIntoElementContent(tmp, (double) v);
      else if (v instanceof Boolean)
        XmlSaveUtils.saveIntoElementContent(tmp, (boolean) v);
      else if (v instanceof Character)
        XmlSaveUtils.saveIntoElementContent(tmp, (char) v);
      else if (v.getClass().isEnum())
        XmlSaveUtils.saveIntoElementContent(tmp, v.toString());
      else if (v instanceof String)
        XmlSaveUtils.saveIntoElementContent(tmp, (String) v);
      else if (v instanceof ETimeStamp)
        XmlSaveUtils.saveIntoElementContent(tmp, ((ETimeStamp) v).toString());
      else if (v instanceof EDayTimeStamp)
        XmlSaveUtils.saveIntoElementContent(tmp, ((EDayTimeStamp) v).toString());
      else if (v instanceof EDayTimeRun)
        XmlSaveUtils.saveIntoElementContent(tmp, ((EDayTimeRun) v).toString());
      else if (v instanceof Callsign)
        XmlSaveUtils.saveIntoElementContent(tmp, ((Callsign) v).toString(false));
      else if (v instanceof Squawk)
        XmlSaveUtils.saveIntoElementContent(tmp, ((Squawk) v).toString());
      else if (v instanceof AtcId)
        XmlSaveUtils.saveIntoElementContent(tmp, ((AtcId) v).getName());
      else if (v instanceof Coordinate)
        XmlSaveUtils.saveIntoElementContent(tmp, (Coordinate) v, new CoordinateFormatter());
      else
        throw new XmlUtilsException(sf("Failed to save type '%s'. This type is not supported by this function.", v.getClass()));
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
      for (String fieldName : fieldNames) {
        storeField(target, object, fieldName);
      }
    }

    public static void storeFields(XElement target, Object object, Iterable<String> fieldNames) {
      for (String fieldName : fieldNames) {
        storeField(target, object, fieldName);
      }
    }
  }

  public static class Class {
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
