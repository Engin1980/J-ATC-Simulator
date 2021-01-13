package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class ShortcutCommand extends ToNavaidCommand {

  public ShortcutCommand(String navaidName) {
    super(navaidName);
  }

  @XConstructor
  @XmlConstructor
  private ShortcutCommand() {
    super("?");
  }

  @Override
  public String toString() {
    String ret = "Shortcut to " + getNavaidName() + " {command}";

    return ret;
  }

}
