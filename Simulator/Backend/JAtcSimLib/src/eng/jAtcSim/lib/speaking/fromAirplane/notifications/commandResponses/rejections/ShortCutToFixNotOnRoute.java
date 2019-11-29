package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ShortcutCommand;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;

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
