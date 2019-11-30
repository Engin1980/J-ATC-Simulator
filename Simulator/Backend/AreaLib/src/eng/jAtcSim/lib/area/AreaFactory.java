package eng.jAtcSim.lib.area;

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

    Area area = loadArea(doc.getRoot());
    return area;
  }

  private Area loadArea(XElement element) {
    String icao = XmlLoader.loadString(element, "icao");

    NavaidList navaids = Navaid.loadList(
        element.getChild("navaids").getChildren("navaid"));

    IList<Border> borders = Border.loadList(
        element.getChild("borders").getChildren("border"), navaids);

    IList<Airport> airports = new EList<>();
    for (XElement child : element.getChild("airports").getChildren("airport")) {
      Airport airport = Airport.load(child, navaids, borders);
      airports.add(airport);
    }

    Area ret = new Area(icao, airports, navaids, borders);
    return ret;
  }
}
