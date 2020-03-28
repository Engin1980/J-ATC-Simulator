package eng.jAtcSim.newLib.speeches.airplane2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.INotification;

public class DivertingNotification implements INotification {
  private String exitNavaidName;

  public DivertingNotification(String exitNavaidName) {
    EAssert.Argument.isNonEmptyString(exitNavaidName);
    this.exitNavaidName = exitNavaidName;
  }


  public String getExitNavaidName() {
    return exitNavaidName;
  }
}
