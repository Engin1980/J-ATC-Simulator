package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterSpeedCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterSpeedCommandFactory {
  public static AfterSpeedCommand load(XElement element) {
    int speed = XmlLoader.loadInteger("value");
    AfterValuePosition position = Shared.loadAfterValuePosition(element);
    AfterSpeedCommand ret = AfterSpeedCommand.create(speed, position);
    return ret;
  }
}
