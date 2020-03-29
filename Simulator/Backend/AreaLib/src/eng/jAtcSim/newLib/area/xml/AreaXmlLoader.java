package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AreaXmlLoader {
  public Area load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String icao = XmlLoaderUtils.loadString("icao");

    NavaidList navaids = new NavaidList();
    XmlLoaderUtils.loadList(
        source.getChild("navaids").getChildren("navaid"),
        navaids,
        new NavaidXmlLoader()
    );

    IList<Border> borders = XmlLoaderUtils.loadList(
        source.getChild("borders").getChildren("border"),
        new BorderXmlLoader());

    IList<Airport> airports = XmlLoaderUtils.loadList(
        source.getChild("airports").getChildren("airport"),
        new AirportXmlLoader(navaids));

    Area ret = Area.create(icao, navaids, borders, airports);
    return ret;
  }
}
