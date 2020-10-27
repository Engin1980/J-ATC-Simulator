package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class LoadSave {
  public static void saveFieldsIntoElement(XElement target, Object object, String... fieldNames) {
    for (String fieldName : fieldNames) {
      saveFieldIntoElement(target, object, fieldName);
    }
  }

  public static void saveFieldIntoElement(XElement target, Object object, String fieldName) {
    Object v = ObjectUtils.getFieldValue(object, fieldName);

    XElement tmp = new XElement(fieldName);
    target.addElement(tmp);

    if (v == null)
      XmlUtils.saveNullIntoElement(tmp);
    else if (v instanceof Short)
      XmlUtils.saveIntoElementContent(tmp, (short) v);
    else if (v instanceof Byte)
      XmlUtils.saveIntoElementContent(tmp, (byte) v);
    else if (v instanceof Integer)
      XmlUtils.saveIntoElementContent(tmp, (int) v);
    else if (v instanceof Long)
      XmlUtils.saveIntoElementContent(tmp, (long) v);
    else if (v instanceof Float)
      XmlUtils.saveIntoElementContent(tmp, (float) v);
    else if (v instanceof Double)
      XmlUtils.saveIntoElementContent(tmp, (double) v);
    else if (v instanceof Boolean)
      XmlUtils.saveIntoElementContent(tmp, (boolean) v);
    else if (v instanceof Character)
      XmlUtils.saveIntoElementContent(tmp, (char) v);
    else if (v instanceof String)
      XmlUtils.saveIntoElementContent(tmp, (String) v);
    else
      throw new XmlUtilsException(sf("Failed to save type '%s'. This type is not supported by this function.", v.getClass()));
  }

  public static <T> void saveFieldIntoElement(XElement target, Object object, String fieldName, Formatter<T> formatter) {
    Object v = ObjectUtils.getFieldValue(object, fieldName);

    XElement tmp = new XElement(fieldName);
    target.addElement(tmp);

    XmlUtils.saveIntoElementContent(tmp, (T) v, formatter);
  }
}
