package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.sharedLib.xml.XmlLoadException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaFactory {
  public Area create(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load area from '{0}'.", fileName));
    }

    Area area = Area.load(doc.getRoot());
    return area;
  }
}
