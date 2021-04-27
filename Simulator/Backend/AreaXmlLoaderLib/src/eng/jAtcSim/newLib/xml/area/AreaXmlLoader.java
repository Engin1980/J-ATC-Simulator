package eng.jAtcSim.newLib.xml.area;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AreaXmlLoader {
  public static Area load(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new ApplicationException(sf("Failed to load area from '{0}'.", fileName));
    }

    LoadingContext context = new LoadingContext();
    eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader loader =
            new eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader(context);

    Area area = loader.load(doc.getRoot());
    return area;
  }
}
