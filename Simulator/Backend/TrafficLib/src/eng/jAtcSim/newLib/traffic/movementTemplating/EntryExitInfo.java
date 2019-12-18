package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.jAtcSim.newLib.shared.SharedFactory;

public class EntryExitInfo {
  public static EntryExitInfo getRandom() {
    int radial = SharedFactory.getRnd().nextInt(0, 360);
    EntryExitInfo ret = new EntryExitInfo(radial);
    return ret;
  }

  private String navaid;
  private Integer radial;

  public EntryExitInfo(String navaid) {
    if (navaid == null) {
      throw new IllegalArgumentException("Value of {navaid} cannot not be null.");
    }

    this.navaid = navaid;
    this.radial = null;
  }

  public EntryExitInfo(Integer radial) {
    if (radial == null) {
      throw new IllegalArgumentException("Value of {radial} cannot not be null.");
    }

    this.radial = radial;
    this.navaid = null;
  }

  public String getNavaid() {
    return navaid;
  }

  public Integer getRadial() {
    return radial;
  }
}
