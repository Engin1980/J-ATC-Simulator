package eng.jAtcSim.newLib.speeches.airplane2atc.responses;


import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ShortcutCommand;

public class ShortCutToFixNotOnRoute extends Rejection {
  public ShortCutToFixNotOnRoute(ShortcutCommand origin) {
    super(origin,"Unable to shortcut to " + origin.getNavaidName() + ", fix not on our route!");
  }

  @Override
  public String toString(){
    String ret = "Rejection of shortcut. " + super.toString();

    return ret;
  }
}
