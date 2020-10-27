package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.traffic.contextLocal.Context;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.formatters.CoordinateFormatter;

public class EntryExitInfo {
  public static EntryExitInfo getRandom() {
    int radial = Context.getApp().getRnd().nextInt(0, 360);
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

  public void save(XElement target) {
    XmlSaveUtils.Field.storeFields(target, this, "navaid", "radial");
    XmlSaveUtils.Field.storeField(target, this, "otherAirportCoordinate", new CoordinateFormatter());
  }
}
