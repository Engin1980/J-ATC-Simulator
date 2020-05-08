package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

public class ShortcutCommand extends ToNavaidCommand {

  public ShortcutCommand(String navaidName) {
    super(navaidName);
  }

  @Override
  public String toString() {
    String ret = "Shortcut to " + getNavaidName() + " {command}";

    return ret;
  }

}
