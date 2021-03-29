package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterNavaidCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class AfterNavaidCommandXmlLoader extends XmlLoader<AfterDistanceCommand> {

  public AfterNavaidCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public AfterDistanceCommand load(XElement element) {

    SmartXmlLoaderUtils.setContext(element);
    String navaidName = SmartXmlLoaderUtils.loadString("fix");
    AfterNavaidCommand ret = AfterNavaidCommand.create(navaidName);
    return ret;
  }
}
