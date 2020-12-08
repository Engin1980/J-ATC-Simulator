package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterNavaidCommand;

public class AfterNavaidCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {
  @Override
  public AfterDistanceCommand load(XElement element) {

    SmartXmlLoaderUtils.setContext(element);
    String navaidName = SmartXmlLoaderUtils.loadString("fix");
    AfterNavaidCommand ret = AfterNavaidCommand.create(navaidName);
    return ret;
  }
}
