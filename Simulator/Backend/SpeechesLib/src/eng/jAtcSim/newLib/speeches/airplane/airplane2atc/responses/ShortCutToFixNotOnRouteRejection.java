package eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses;


import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ShortcutCommand;

public class ShortCutToFixNotOnRouteRejection extends PlaneRejection {
  public ShortCutToFixNotOnRouteRejection(ShortcutCommand origin) {
    super(origin,"Unable to shortcut to " + origin.getNavaidName() + ", fix not on our route!");
  }

  @Override
  public String toString(){
    String ret = "Rejection of shortcut. " + super.toString();

    return ret;
  }
}
