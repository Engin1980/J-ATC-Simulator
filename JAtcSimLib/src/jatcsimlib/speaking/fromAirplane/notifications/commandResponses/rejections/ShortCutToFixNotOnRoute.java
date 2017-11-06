package jatcsimlib.speaking.fromAirplane.notifications.commandResponses.rejections;

import jatcsimlib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import jatcsimlib.speaking.fromAtc.commands.ShortcutCommand;

public class ShortCutToFixNotOnRoute extends Rejection {
  public ShortCutToFixNotOnRoute(ShortcutCommand origin) {
    super("Unable to shortcut to " + origin.getNavaid().getName() + ", fix not on our route!", origin);
  }

  @Override
  public String toString(){
    String ret = "Rejection of shortcut. " + super.toString();

    return ret;
  }
}
