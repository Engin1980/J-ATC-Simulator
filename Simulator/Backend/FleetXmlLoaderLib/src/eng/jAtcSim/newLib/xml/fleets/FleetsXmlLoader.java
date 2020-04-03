package eng.jAtcSim.newLib.xml.fleets;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.fleet.Fleets;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.xml.fleets.internal.Context;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class FleetsXmlLoader {
  public static Fleets create(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load area from '{0}'.", fileName));
    }

    eng.jAtcSim.newLib.xml.fleets.internal.FleetsXmlLoader loader =
        new eng.jAtcSim.newLib.xml.fleets.internal.FleetsXmlLoader();

    Fleets ret = loader.load(doc.getRoot());
    return ret;
  }
}
