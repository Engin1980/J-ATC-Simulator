package eng.jAtcSim.newLib.xml.area.internal.routes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoaderWithNavaids;
import eng.jAtcSim.newLib.xml.area.internal.XmlMappingDictinary;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

public class GaRouteXmlLoader extends XmlLoader<GaRoute> {

  public GaRouteXmlLoader(Context context) {
    super(context);
  }

  @Override
  public GaRoute load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String mapping = XmlLoaderUtils.loadString("gaMapping");

    IList<ICommand> commands = XmlLoaderUtils.loadList(
        source.getChildren(),
        new SpeechXmlLoader()
    );

    GaRoute ret = new GaRoute(commands);
    context.airport.gaMappings.add(mapping, ret);
    return ret;
  }
}
