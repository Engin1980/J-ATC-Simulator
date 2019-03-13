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
  private final String name;
  private final Navaid navaid;
  private final Type type;
  private final Integer maxMrvaAltitude;
  private final int radialFromAirport;

  public EntryExitPoint(Airport parent, String name, Navaid navaid, Type type, Integer maxMrvaAltitude, int radialFromAirport) {
    this.parent = parent;
    this.name = name;
    this.navaid = navaid;
    this.type = type;
    this.maxMrvaAltitude = maxMrvaAltitude;
    this.radialFromAirport = radialFromAirport;
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
//    if ((eep.type == Type.both && this.type != Type.both) ||
//        (this.type == Type.entry && eep.type == Type.exit) ||
//        (this.type == Type.exit && eep.type == Type.entry))
//      this.type = Type.both;
  }

  @Override
  public String toString() {
    return  this.name + " (" + this.type + ") {entryExitPoint}";
  }

  public String getName() {
    return this.name;
  }

  public int getRadialFromAirport() {
    return radialFromAirport;
  }
}
