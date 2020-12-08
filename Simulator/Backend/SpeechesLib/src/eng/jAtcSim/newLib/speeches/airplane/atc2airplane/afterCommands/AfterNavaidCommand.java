package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;


import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.newXmlUtils.annotations.XmlConstructor;

/**
 * Similar to AfterDistance, but this represents situation when plane is orderet to fly directly to navaid.
 */
public class AfterNavaidCommand extends AfterDistanceCommand {

  public static AfterNavaidCommand create(String name){
    return new AfterNavaidCommand(name);
  }

  @XmlConstructor
  private AfterNavaidCommand() {
    super("?", 0, AboveBelowExactly.exactly);
  }

  private AfterNavaidCommand(String navaidName) {
    super(navaidName, 0, AboveBelowExactly.exactly);
  }
}
