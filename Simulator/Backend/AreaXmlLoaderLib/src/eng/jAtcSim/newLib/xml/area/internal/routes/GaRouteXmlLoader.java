package eng.jAtcSim.newLib.xml.area.internal.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

public class GaRouteXmlLoader extends XmlLoader<GaRoute> {

  public GaRouteXmlLoader(Context context) {
    super(context);
  }

  @Override
  public GaRoute load(XElement source) {
    log(2, "Xml-loading ga-route");
    SmartXmlLoaderUtils.setContext(source);
    String mapping = SmartXmlLoaderUtils.loadString("gaMapping");
    log(3, "... ga-route ga-mapping '%s'", mapping);

    IList<ICommand> commands = SmartXmlLoaderUtils.loadList(
        source.getChildren(),
        new SpeechXmlLoader()
    );

    GaRoute ret = new GaRoute(commands);
    context.airport.gaMappings.add(mapping, ret);
    return ret;
  }
}
