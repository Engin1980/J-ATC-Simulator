package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.eSystem.geo.Coordinate;

public class EntryExitPoint {
  public enum Type {
    entry,
    exit,
    both
  }

  private final Airport parent;
  private final Navaid navaid;
  private final Type type;
  private final Integer maxMrvaAltitude;
  private final int radialFromAirport;

  public EntryExitPoint(Airport parent, Navaid navaid, Type type, Integer maxMrvaAltitude) {
    this.parent = parent;
    this.navaid = navaid;
    this.type = type;
    this.maxMrvaAltitude = maxMrvaAltitude;
    this.radialFromAirport = (int) Math.round(Coordinates.getBearing(parent.getLocation(), navaid.getCoordinate()));
  }

  public Airport getParent() {
    return parent;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public Type getType() {
    return type;
  }

  public int getMaxMrvaAltitudeOrHigh() {
    if (maxMrvaAltitude == null)
      return 0;
    else
      return maxMrvaAltitude;
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

  @Override
  public String toString() {
    return  this.getName() + " (" + this.type + ") {entryExitPoint}";
  }

  public String getName() {
    return this.navaid.getName();
  }

  public int getRadialFromAirport() {
    return radialFromAirport;
  }
}
