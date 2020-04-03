package eng.jAtcSim.newLib.xml.traffic;

import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.traffic.Traffic;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TrafficXmlLoader {
  public static Traffic create(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load traffic from '{0}'.", fileName));
    }

    eng.jAtcSim.newLib.xml.traffic.internal.TrafficXmlLoader loader =
        new eng.jAtcSim.newLib.xml.traffic.internal.TrafficXmlLoader();

    Traffic ret = loader.load(doc.getRoot());
    return ret;
  }
}
