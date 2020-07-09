package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterSpeedCommand;

public class AfterSpeedCommandXmlLoader implements IXmlLoader<AfterSpeedCommand> {
  @Override
  public AfterSpeedCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);
    int value = SmartXmlLoaderUtils.loadInteger("value");
    AboveBelowExactly extension = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterSpeedCommand ret = AfterSpeedCommand.create(value, extension);
    return ret;
  }
}
