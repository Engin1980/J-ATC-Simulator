package eng.jAtcSim.newLib.xml.area;

import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaXmlLoader {
  public static Area create(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load area from '{0}'.", fileName));
    }

    Context context = new Context();
    eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader loader =
        new eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader(context);

    Area area = loader.load(doc.getRoot());
    return area;
  }
}
