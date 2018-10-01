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

  @XmlIgnore
  private Airport parent;
  private String name;
  @XmlIgnore
  private Navaid navaid;
  private Type type;
  @XmlIgnore
  private Integer maxMrvaAltitude = null;
  @XmlIgnore
  private int radialFromAirport;

  public EntryExitPoint(Navaid mainFix, Type type, int maxMrvaAltitude) {
    this.navaid = mainFix;
    this.name = this.navaid.getName();
    this.type = type;
    this.maxMrvaAltitude = maxMrvaAltitude;
  }

  private EntryExitPoint() {
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

  public void bind() {
    this.parent = Acc.airport();
    this.navaid = Acc.area().getNavaids().get(this.name);

    Area area = this.getParent().getParent();
    IList<Border> mrvas = area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    Tuple<Coordinate, Coordinate> line =
        new Tuple<>(
            this.navaid.getCoordinate(),
            this.getParent().getMainAirportNavaid().getCoordinate()
        );

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (mrva.hasIntersectionWithLine(line))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }

    if (this.maxMrvaAltitude == null)
      this.maxMrvaAltitude = maxMrvaAlt;
    else
      this.maxMrvaAltitude = Math.min(this.maxMrvaAltitude, maxMrvaAlt);

    this.radialFromAirport = (int) Math.round(
        Coordinates.getBearing(this.parent.getLocation(), this.navaid.getCoordinate()));
  }

  public void adjustBy(EntryExitPoint eep) {
    assert this.navaid.equals(eep.navaid);

    this.maxMrvaAltitude = Math.min(this.maxMrvaAltitude, eep.maxMrvaAltitude);
    if ((eep.type == Type.both && this.type != Type.both) ||
        (this.type == Type.entry && eep.type == Type.exit) ||
        (this.type == Type.exit && eep.type == Type.entry))
      this.type = Type.both;
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
