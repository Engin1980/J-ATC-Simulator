package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaFactory {
  public Area create(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load area from '{0}'.", fileName));
    }

    Area area = Area.XmlLoader.load(doc.getRoot());
    return area;
  }
}
