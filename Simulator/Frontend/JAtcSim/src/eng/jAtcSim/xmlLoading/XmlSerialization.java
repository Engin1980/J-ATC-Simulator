package eng.jAtcSim.xmlLoading;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.validation.EAssert;
import eng.eXmlSerialization.XmlSerializer;

import java.io.File;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class XmlSerialization {

  public static <T> T load(XmlSerializer serializer, XElement source, Class<T> type) {
    EAssert.Argument.isNotNull(serializer, "serializer");
    EAssert.Argument.isNotNull(source, "source");
    EAssert.Argument.isNotNull(type, "type");

    T ret;

    try {
      ret = serializer.deserialize(source, type);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to deserialize/load '%s' from '%s'.", type, source), e);
    }

    return ret;
  }

  public static <T> T loadFromFile(XmlSerializer serializer, String fileName, Class<T> type) {
    EAssert.Argument.isNotNull(fileName, "fileName");
    File file = new File(fileName);
    T ret = loadFromFile(serializer, file, type);
    return ret;
  }

  public static <T> T loadFromFile(XmlSerializer serializer, File fileName, Class<T> type) {
    EAssert.Argument.isNotNull(fileName, "fileName");

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Unable to load xml file '%s'.", fileName), e);
    }

    T ret = load(serializer, doc.getRoot(), type);

    return ret;
  }

  public static <T> void save(XmlSerializer serializer, T value, Class<? super T> valueType, XElement target) {
    EAssert.Argument.isNotNull(serializer, "serializer");
    EAssert.Argument.isNotNull(valueType, "valueType");
    EAssert.Argument.isNotNull(target, "target");

    try {
      serializer.serialize(value, target);
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to serialize/save '%s' into '%s'.", value, target), e);
    }
  }

  public static <T> XElement save(XmlSerializer serializer, T value, Class<? super T> valueType, String targetElementName) {
    EAssert.Argument.isNotNull(targetElementName, "targetElementName");
    XElement ret = new XElement(targetElementName);
    save(serializer, value, valueType, ret);
    return ret;
  }

  public static <T> void saveToFile(XmlSerializer serializer, T value, Class<? super T> valueType, String fileName) {
    saveToFile(serializer, value, valueType, fileName, "root");
  }

  public static <T> void saveToFile(XmlSerializer serializer, T value, Class<? super T> valueType, String fileName, String rootElementName) {
    EAssert.Argument.isNotNull(fileName, "fileName");

    XElement rootElement = new XElement(rootElementName);
    save(serializer, value, valueType, rootElement);
    XDocument doc = new XDocument(rootElement);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException(sf("Failed to save xml file '%s'.", fileName), e);
    }
  }
}
