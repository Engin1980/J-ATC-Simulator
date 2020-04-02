package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterHeadingCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterHeadingCommandXmlLoader implements IXmlLoader<AfterHeadingCommand> {
  @Override
  public AfterHeadingCommand load(XElement element) {
    int heading = XmlLoaderUtils.loadInteger("heading");
    AfterValuePosition position = Shared.loadAfterValuePosition(element);
    AfterHeadingCommand ret = AfterHeadingCommand.create(heading, position);
    return ret;
  }
}
