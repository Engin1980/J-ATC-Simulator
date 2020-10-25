package eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;


import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

/**
 * Similar to AfterDistance, but this represents situation when plane is orderet to fly directly to navaid.
 */
public class AfterNavaidCommand extends AfterDistanceCommand {

  public static AfterNavaidCommand create(String navaidName) {
    AfterNavaidCommand ret = new AfterNavaidCommand(navaidName);
    return ret;
  }

  public AfterNavaidCommand(String navaidName) {
    super(navaidName, 0, AboveBelowExactly.exactly);
  }
}
