package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;

public class AfterNavaidCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {
  @Override
  public AfterDistanceCommand load(XElement element) {

    SmartXmlLoaderUtils.setContext(element);
    String navaidName = SmartXmlLoaderUtils.loadString("fix");
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, 0, AboveBelowExactly.exactly);
    return ret;
  }
}
