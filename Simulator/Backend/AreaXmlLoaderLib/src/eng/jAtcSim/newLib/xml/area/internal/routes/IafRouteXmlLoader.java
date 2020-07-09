package eng.jAtcSim.newLib.xml.area.internal.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

public class IafRouteXmlLoader extends XmlLoader<IafRoute> {

  public IafRouteXmlLoader(Context context) {
    super(context);
  }

  @Override
  public IafRoute load(XElement source) {
    log(2, "Xml-loading iaf-route");
    SmartXmlLoaderUtils.setContext(source);
    String iafName = SmartXmlLoaderUtils.loadString("iaf");
    log(3, "... iaf-route '%s'", iafName);
    Navaid navaid = context.area.navaids.get(iafName);
    PlaneCategoryDefinitions category = SmartXmlLoaderUtils.loadPlaneCategory("category", "ABCD");
    String mapping = SmartXmlLoaderUtils.loadString("iafMapping");

    IList<ICommand> commands = SmartXmlLoaderUtils.loadList(
        source.getChildren(),
        new SpeechXmlLoader()
    );

    IafRoute ret = IafRoute.create(commands, navaid, category);
    context.airport.iafMappings.add(mapping, ret);
    return ret;
  }
}
