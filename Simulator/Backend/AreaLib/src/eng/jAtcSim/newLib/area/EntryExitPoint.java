package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;

import exml.annotations.XConstructor;

public class EntryExitPoint extends Parentable<Airport> {

  public enum Type {
    entry,
    exit,
    both;

    public boolean is(Type type){
      return this == type || this == both;
    }
  }

  private static final int UNSET = -1;

  public static EntryExitPoint create(Navaid navaid, Type type, Integer maxMrvaAltitude) {
    EntryExitPoint ret = new EntryExitPoint(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private Navaid navaid;
  private Type type;
  private Integer maxMrvaAltitude;
  private int radialFromAirport = UNSET;


  @XConstructor
  private EntryExitPoint() {
  }

  private EntryExitPoint(Navaid navaid, Type type, Integer maxMrvaAltitude) {
    this.navaid = navaid;
    this.type = type;
    this.maxMrvaAltitude = maxMrvaAltitude;
  }

  public void adjustBy(EntryExitPoint eep) {
    EAssert.Argument.isTrue(this.navaid.equals(eep.navaid));

    this.maxMrvaAltitude = Math.min(this.maxMrvaAltitude, eep.maxMrvaAltitude);
    if ((eep.type == Type.both && this.type != Type.both) ||
        (this.type == Type.entry && eep.type == Type.exit) ||
        (this.type == Type.exit && eep.type == Type.entry))
      this.type = Type.both;
  }

  public int getMaxMrvaAltitudeOrHigh() {
    if (maxMrvaAltitude == null)
      return 0;
    else
      return maxMrvaAltitude;
  }

  public String getName() {
    return this.navaid.getName();
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public int getRadialFromAirport() {
    if (radialFromAirport == UNSET)
      this.radialFromAirport = (int) Math.round(Coordinates.getBearing(
              super.getParent().getLocation(), navaid.getCoordinate()));
    return radialFromAirport;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return this.getName() + " (" + this.type + ") {entryExitPoint}";
  }
}
