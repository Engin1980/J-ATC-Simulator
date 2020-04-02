package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterNavaidCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {
  @Override
  public AfterDistanceCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    double distance = 0;
    AfterValuePosition position = AfterValuePosition.exactly;
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, position);
    return ret;
  }
}
