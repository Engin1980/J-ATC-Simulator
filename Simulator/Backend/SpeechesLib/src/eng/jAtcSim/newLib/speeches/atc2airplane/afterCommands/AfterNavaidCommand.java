package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;

public class AfterNavaidCommand extends AfterCommandWithNavaid {

  public static AfterNavaidCommand load(XElement element, Airport parent) {
    AfterNavaidCommand ret = new AfterNavaidCommand();
    ret.read(element, parent);
    return ret;
  }

  private AfterNavaidCommand() {
  }
}
