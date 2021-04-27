package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import exml.annotations.XConstructor;

public class ShortcutCommand extends ToNavaidCommand {

  public static ShortcutCommand create(String navaidName) {
    return new ShortcutCommand(navaidName);
  }

  @XConstructor
  private ShortcutCommand() {
    super("?");
  }

  private ShortcutCommand(String navaidName) {
    super(navaidName);
  }

  @Override
  public String toString() {
    String ret = "Shortcut to " + getNavaidName() + " {command}";

    return ret;
  }

}
