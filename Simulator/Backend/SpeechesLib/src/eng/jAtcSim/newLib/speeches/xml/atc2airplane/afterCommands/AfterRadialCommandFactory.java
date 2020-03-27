package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterRadialCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterRadialCommandFactory {
  public static AfterRadialCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    int radial = XmlLoader.loadInteger(element, "radial");
    AfterValuePosition extension = Shared.loadAfterValuePosition(element);
    AfterRadialCommand ret = AfterRadialCommand.create(navaidName, radial, extension);
    return ret;
  }
}
