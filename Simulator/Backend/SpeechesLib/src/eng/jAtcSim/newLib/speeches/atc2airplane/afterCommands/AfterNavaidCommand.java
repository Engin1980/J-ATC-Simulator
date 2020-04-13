package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;


import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;

public class AfterNavaidCommand extends AfterDistanceCommand {

  public static AfterNavaidCommand create(String navaidName) {
    AfterNavaidCommand ret = new AfterNavaidCommand(navaidName);
    return ret;
  }

  public AfterNavaidCommand(String navaidName) {
    super(navaidName, 0, AboveBelowExactly.exactly);
  }
}
