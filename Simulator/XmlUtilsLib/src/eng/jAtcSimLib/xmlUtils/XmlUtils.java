package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;

public class XmlUtils {
  private static final String NULL_STRING = "(null)";

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
      saveNullIntoElement(target);
    else
      target.setContent(value);
  }

  public static <T> void saveIntoElementContent(XElement target, T value, Formatter<T> formatter) {
    String s = formatter.invoke(value);
    saveIntoElementContent(target, s);
  }

  public static void saveIntoElementContent(XElement target, Number value) {
    if (value == null)
      saveNullIntoElement(target);
    else
      saveIntoElementContent(target, value.toString());
  }

  public static void saveNullIntoElement(XElement target) {
    target.setContent(NULL_STRING);
  }
}
