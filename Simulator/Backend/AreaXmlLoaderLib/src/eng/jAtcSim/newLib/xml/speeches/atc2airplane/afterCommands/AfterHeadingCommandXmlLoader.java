package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;

public class AfterHeadingCommandXmlLoader implements IXmlLoader<AfterHeadingCommand> {
  @Override
  public AfterHeadingCommand load(XElement element) {
    int heading = XmlLoaderUtils.loadInteger("heading");
    AfterHeadingCommand ret = AfterHeadingCommand.create(heading);
    return ret;
  }
}
