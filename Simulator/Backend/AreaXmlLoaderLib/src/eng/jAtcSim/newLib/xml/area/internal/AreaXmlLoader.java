package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class AreaXmlLoader extends XmlLoader {
  public AreaXmlLoader(Context context) {
    super(context);
  }

  public Area load(XElement source) {
    XmlLoaderUtils.setContext(source);
    context.area.icao = XmlLoaderUtils.loadString("icao");

    context.area.navaids = new NavaidList();
    XmlLoaderUtils.loadList(
        source.getChild("navaids").getChildren("navaid"),
        context.area.navaids,
        new NavaidXmlLoader()
    );

    context.area.borders = XmlLoaderUtils.loadList(
        source.getChild("borders").getChildren("border"),
        new BorderXmlLoader());

    IList<Airport> airports = XmlLoaderUtils.loadList(
        source.getChild("airports").getChildren("airport"),
        new AirportXmlLoader(context));

    Area ret = Area.create(
        context.area.icao,
        airports,
        context.area.navaids,
        context.area.borders);
    return ret;
  }
}
