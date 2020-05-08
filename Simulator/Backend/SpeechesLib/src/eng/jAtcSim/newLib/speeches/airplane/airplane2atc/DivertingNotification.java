package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.INotification;

public class DivertingNotification implements INotification {
  private final String exitNavaidName;

  public DivertingNotification(String exitNavaidName) {
    EAssert.Argument.isNonemptyString(exitNavaidName);
    this.exitNavaidName = exitNavaidName;
  }


  public String getExitNavaidName() {
    return exitNavaidName;
  }
}
