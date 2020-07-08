package eng.jAtcSim.newLib.xml.fleets;

import eng.eSystem.eXml.XDocument;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.xml.fleets.internal.AirlinesFleetsXmlLoader;
import eng.jAtcSim.newLib.xml.fleets.internal.GeneralAviationFleetsXmlLoader;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FleetsXmlLoader {

  public static AirlinesFleets loadAirlinesFleets(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load airlines fleets from '{0}'.", fileName));
    }

    AirlinesFleetsXmlLoader loader =
        new AirlinesFleetsXmlLoader();

    AirlinesFleets ret = loader.load(doc.getRoot());
    return ret;
  }

  public static GeneralAviationFleets loadGeneralAviationFleets(String fileName) {
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (Exception ex) {
      throw new XmlLoadException(sf("Failed to load G-A fleets from '{0}'.", fileName));
    }

    GeneralAviationFleetsXmlLoader loader =
        new GeneralAviationFleetsXmlLoader();

    GeneralAviationFleets ret = loader.load(doc.getRoot());
    return ret;
  }
}
