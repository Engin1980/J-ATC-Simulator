package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;

public class AfterHeadingCommandXmlLoader implements IXmlLoader<AfterHeadingCommand> {
  @Override
  public AfterHeadingCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);
    int value = SmartXmlLoaderUtils.loadInteger("value");
    AboveBelowExactly extension = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterHeadingCommand ret = AfterHeadingCommand.create(value, extension);
    return ret;
  }
}
