package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterNavaidCommandFactory {
  public static AfterDistanceCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    double distance = 0;
    AfterValuePosition position = AfterValuePosition.exactly;
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, position);
    return ret;
  }
}
