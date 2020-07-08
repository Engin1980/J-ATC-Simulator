package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class AreaXmlLoader extends XmlLoader<Area> {
  public AreaXmlLoader(Context context) {
    super(context);
  }

  @Override
  public Area load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    context.area.icao = SmartXmlLoaderUtils.loadString("icao");

    context.area.navaids = new NavaidList();
    AreaAcc.setNavaidsProducer(() -> context.area.navaids);
    SmartXmlLoaderUtils.loadList(
        source.getChild("navaids").getChildren("navaid"),
        context.area.navaids,
        new NavaidXmlLoader()
    );

    context.area.borders = SmartXmlLoaderUtils.loadList(
        source.getChild("borders").getChildren("border"),
        new BorderXmlLoader());

    IList<Airport> airports = SmartXmlLoaderUtils.loadList(
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
