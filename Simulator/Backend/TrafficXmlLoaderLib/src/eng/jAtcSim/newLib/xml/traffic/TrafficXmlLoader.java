package eng.jAtcSim.newLib.xml.traffic;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.traffic.ITrafficModel;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TrafficXmlLoader {
  public static ITrafficModel load(String fileName) {
    ITrafficModel ret;
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
      ret = eng.jAtcSim.newLib.xml.traffic.internal.TrafficXmlLoader.load(doc.getRoot());
    } catch (Exception ex) {
      throw new ApplicationException(sf("Failed to load traffic from '%s'.", fileName), ex);
    }

    return ret;
  }
}
