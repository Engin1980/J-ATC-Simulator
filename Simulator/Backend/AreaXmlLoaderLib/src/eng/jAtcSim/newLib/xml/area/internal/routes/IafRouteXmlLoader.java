package eng.jAtcSim.newLib.xml.area.internal.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

public class IafRouteXmlLoader extends XmlLoader<IafRoute> {

  public IafRouteXmlLoader(Context context) {
    super(context);
  }

  @Override
  public IafRoute load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String iafName = XmlLoaderUtils.loadString("iaf");
    Navaid navaid = context.area.navaids.get(iafName);
    PlaneCategoryDefinitions category = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");
    String mapping = XmlLoaderUtils.loadString("iafMapping");

    IList<ICommand> commands = XmlLoaderUtils.loadList(
        source.getChildren(),
        new SpeechXmlLoader()
    );

    IafRoute ret = IafRoute.create(commands, navaid, category);
    context.airport.iafMappings.add(mapping, ret);
    return ret;
  }
}
