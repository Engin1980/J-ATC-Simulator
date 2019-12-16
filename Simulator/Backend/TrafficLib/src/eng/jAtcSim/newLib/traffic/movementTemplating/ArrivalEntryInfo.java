package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.collections.*;

public class ArrivalEntryInfo {
  private String navaid;
  private Integer radial;

  public ArrivalEntryInfo(String navaid) {
    if (navaid == null) {
        throw new IllegalArgumentException("Value of {navaid} cannot not be null.");
    }

    this.navaid = navaid;
    this.radial = null;
  }

  public ArrivalEntryInfo(Integer radial) {
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
