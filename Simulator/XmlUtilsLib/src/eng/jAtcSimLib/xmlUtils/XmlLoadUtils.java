package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;

public class XmlLoadUtils {
  public static class Field{

    public static void loadField(XElement element, Object target, String fieldName, Parser parser) {
      loadField(element, target, fieldName, parser.toDeserializer());
    }

    public static void loadField(XElement element, Object target, String fieldName, Deserializer parser) {
      throw new ToDoException();
    }
  }
}
