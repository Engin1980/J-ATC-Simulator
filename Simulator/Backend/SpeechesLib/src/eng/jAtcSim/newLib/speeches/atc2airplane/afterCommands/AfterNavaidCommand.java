package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;


public class AfterNavaidCommand extends AfterDistanceCommand {

  public static AfterNavaidCommand create(String navaidName) {
    AfterNavaidCommand ret = new AfterNavaidCommand(navaidName);
    return ret;
  }

  public AfterNavaidCommand(String navaidName) {
    super(navaidName, 0, AfterValuePosition.exactly);
  }
}
