package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterSpeedCommand;

public class AfterSpeedCommandXmlLoader implements IXmlLoader<AfterSpeedCommand> {
  @Override
  public AfterSpeedCommand load(XElement element) {
    int speed = XmlLoaderUtils.loadInteger("value");
    AboveBelowExactly position = Shared.loadAfterValuePosition(element);
    AfterSpeedCommand ret = AfterSpeedCommand.create(speed, position);
    return ret;
  }
}
