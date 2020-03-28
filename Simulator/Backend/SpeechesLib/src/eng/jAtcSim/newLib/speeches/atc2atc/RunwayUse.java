package eng.jAtcSim.newLib.speeches.atc2atc;

import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.IRejectable;

public class RunwayUse implements INotification, IRejectable {

  private boolean asksForChange;

  public RunwayUse(boolean asksForChange) {
    this.asksForChange = asksForChange;
  }

  public boolean isAsksForChange() {
    return asksForChange;
  }

  @Override
  public boolean isRejection() {
    return false;
  }

  @Override
  public String toString() {
    return "Runway use{}";
  }
}
