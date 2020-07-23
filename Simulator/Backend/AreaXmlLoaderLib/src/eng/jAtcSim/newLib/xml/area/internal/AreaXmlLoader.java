package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingAreaAcc;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class AreaXmlLoader extends XmlLoader<Area> {
  public AreaXmlLoader(LoadingContext context) {
    super(context);
  }

  @Override
  public Area load(XElement source) {
    super.log(0, "Xml-loading area");
    SmartXmlLoaderUtils.setContext(source);
    context.area.icao = SmartXmlLoaderUtils.loadString("icao");
    super.log(0, "... area '%s'", context.area.icao);

    context.area.navaids = new NavaidList();

    IAreaAcc areaAcc = new LoadingAreaAcc(context.area.navaids);
    ContextManager.setContext(IAreaAcc.class, areaAcc);
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

    super.log(0, "... area '%s' loading completed, compiling", context.area.icao);

    Area ret = Area.create(
        context.area.icao,
        airports,
        context.area.navaids,
        context.area.borders);

    ContextManager.clearContext(IAreaAcc.class);

    super.log(0, "... area '%s' loading done", context.area.icao);
    return ret;
  }
}
