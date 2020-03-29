package eng.jAtcSim.newLib.area;

import eng.eSystem.geo.Coordinates;

public class EntryExitPoint extends Parentable<Airport> {

  public enum Type {
    entry,
    exit,
    both
  }

  public static EntryExitPoint create(Navaid navaid, Type type, Integer maxMrvaAltitude) {
    EntryExitPoint ret = new EntryExitPoint(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private Navaid navaid;
  private Type type;
  private Integer maxMrvaAltitude;
  private int radialFromAirport;

  //TODO delete if not used!
  public EntryExitPoint() {
  }

  private EntryExitPoint(Navaid navaid, Type type, Integer maxMrvaAltitude) {
    this.navaid = navaid;
    this.type = type;
    this.maxMrvaAltitude = maxMrvaAltitude;
    super.getOnParentSet().add(
        () -> this.radialFromAirport = (int) Math.round(Coordinates.getBearing(
            super.getParent().getLocation(), navaid.getCoordinate()))
    );
  }

  public void adjustBy(EntryExitPoint eep) {
    throw new UnsupportedOperationException("I dont know what is this used to do. Check and apply appropriatelly.");
//    assert this.navaid.equals(eep.navaid);
//
//    this.maxMrvaAltitude = Math.min(this.maxMrvaAltitude, eep.maxMrvaAltitude);
//    if ((eep.kind == Type.both && this.kind != Type.both) ||
//        (this.kind == Type.entry && eep.kind == Type.exit) ||
//        (this.kind == Type.exit && eep.kind == Type.entry))
//      this.kind = Type.both;
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
