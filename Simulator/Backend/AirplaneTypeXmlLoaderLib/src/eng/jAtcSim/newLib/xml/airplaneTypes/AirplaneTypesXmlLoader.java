package eng.jAtcSim.newLib.xml.airplaneTypes;

import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplaneTypesXmlLoader {
  public static AirplaneTypes load(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load airplane types from '{0}'.", fileName));
    }

    eng.jAtcSim.newLib.xml.airplaneTypes.internal.AirplaneTypesXmlLoader loader =
        new eng.jAtcSim.newLib.xml.airplaneTypes.internal.AirplaneTypesXmlLoader();

    AirplaneTypes ret = loader.load(doc.getRoot());
    return ret;
  }
}
