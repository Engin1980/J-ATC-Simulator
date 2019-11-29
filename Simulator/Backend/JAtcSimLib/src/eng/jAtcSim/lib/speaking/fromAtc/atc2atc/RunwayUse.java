package eng.jAtcSim.lib.speaking.fromAtc.atc2atc;

import eng.jAtcSim.lib.speaking.fromAtc.IAtc2Atc;

public class RunwayUse implements IAtc2Atc {

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
