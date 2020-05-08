package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;

public class AfterNavaidCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {
  @Override
  public AfterDistanceCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    double distance = 0;
    AboveBelowExactly position = AboveBelowExactly.exactly;
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, position);
    return ret;
  }
}
