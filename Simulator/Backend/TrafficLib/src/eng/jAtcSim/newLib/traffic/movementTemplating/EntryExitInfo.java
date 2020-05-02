package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.SharedAcc;

public class EntryExitInfo {
  public static EntryExitInfo getRandom() {
    int radial = SharedAcc.getRnd().nextInt(0, 360);
    EntryExitInfo ret = new EntryExitInfo(radial);
    return ret;
  }

  private final String navaid;
  private final Integer radial;
  private final Coordinate otherAirportCoordinate;

  public EntryExitInfo(Coordinate otherAirportCoordinate) {
    EAssert.isNotNull(otherAirportCoordinate);
    this.otherAirportCoordinate = otherAirportCoordinate;
    this.navaid = null;
    this.radial = null;
  }

  public EntryExitInfo(String navaid) {
    EAssert.isNotNull(navaid);
    this.navaid = navaid;
    this.radial = null;
    this.otherAirportCoordinate = null;
  }

  public EntryExitInfo(Integer radial) {
    EAssert.isNotNull(radial);

    this.radial = radial;
    this.navaid = null;
    this.otherAirportCoordinate = null;
  }

  public String getNavaid() {
    return navaid;
  }

  public Coordinate getOtherAirportCoordinate() {
    return otherAirportCoordinate;
  }

  public Integer getRadial() {
    return radial;
  }
}
