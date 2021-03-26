package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.jAtcSim.newLib.shared.enums.LeftRight;

import exml.annotations.XConstructor;

public class ShortcutCommand extends ToNavaidCommand {

  public ShortcutCommand(String navaidName) {
    super(navaidName);
  }

  @XConstructor

  private ShortcutCommand() {
    super("?");
  }

  @Override
  public String toString() {
    String ret = "Shortcut to " + getNavaidName() + " {command}";

    return ret;
  }

}
