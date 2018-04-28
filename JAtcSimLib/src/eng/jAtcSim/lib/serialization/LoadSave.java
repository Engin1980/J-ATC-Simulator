package eng.jAtcSim.lib.serialization;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.Settings;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;

import java.lang.reflect.Field;

public class LoadSave {

  private static XmlSerializer ser;

  static {
    Settings sett = new Settings();
    sett.getElementParsers().add(new NavaidParser());
    sett.getElementParsers().add(new AirplaneTypeParser());
    sett.getElementParsers().add(new RunwayThresholdParser());
    sett.getElementParsers().add(new RunwayParser());
    sett.getElementParsers().add(new RouteParser());
    sett.getElementParsers().add(new AirplaneParser());
    sett.getElementParsers().add(new AtcParser());

    sett.getIgnoredFieldsRegex().add("this\\$0"); // parent of inner class

    sett.setVerbose(true);
    ser = new XmlSerializer(sett);
  }

  public static void saveField(XElement elm, Object src, String fieldName) {
    Object v = getFieldValue(src, fieldName);
    if (v == null || v.getClass().isPrimitive() || v.getClass().isEnum())
      LoadSave.saveAsAttribute(elm, fieldName, v);
    else
      LoadSave.saveAsElement(elm, fieldName, v);
  }

  public static void saveAsAttribute(XElement elm, String name, Object value) {
    if (value == null)
      elm.setAttribute(name, "(null)");
    else
      elm.setAttribute(name, value.toString());
  }

  public static void saveAsElement(XElement elm, String name, Object obj) {
    XElement tmp = new XElement(name);
    elm.addElement(tmp);
    try {
      ser.serialize(tmp, obj);
    } catch (XmlSerializationException e) {
      throw new EApplicationException("Failed to save object " + obj + ".", e);
    }
  }

  private static Object getFieldValue(Object src, String fieldName) {
    Class cls = src.getClass();
    Field f;
    Object v;
    try {
      f = getField(cls, fieldName);
      f.setAccessible(true);
      v = f.get(src);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
        throw new EApplicationException("Unreadable field " + fieldName + " on object " + src.getClass(), ex);
    }
    return v;
  }

  private static Field getField(Class type, String name) throws NoSuchFieldException {
    Field f;
    try {
      f = type.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      if (type.equals(Object.class))
        throw e;
      else {
        type = type.getSuperclass();
        f = getField(type, name);
      }
    }
    return f;
  }
}
