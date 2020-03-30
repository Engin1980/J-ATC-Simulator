package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterSpeedCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterSpeedCommandXmlLoader implements IXmlLoader<AfterSpeedCommand> {
  @Override
  public AfterSpeedCommand load(XElement element) {
    int speed = XmlLoaderUtils.loadInteger("value");
    AfterValuePosition position = Shared.loadAfterValuePosition(element);
    AfterSpeedCommand ret = AfterSpeedCommand.create(speed, position);
    return ret;
  }
}