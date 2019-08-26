package eng.jAtcSim.lib.world;

import com.sun.corba.se.impl.monitoring.MonitoredAttributeInfoFactoryImpl;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.world.xml.XmlLoader;

public class EntryExitPoint extends Parentable<Airport> {

  public enum Type {
    entry,
    exit,
    both
  }

  public static EntryExitPointList loadList(IReadOnlyList<XElement> sources, NavaidList navaids,
                                            Navaid airportMainNavaid,
                                            IReadOnlyList<Border> mrvas) {
    IList<EntryExitPoint> tmp = new EList<>();

    for (XElement source : sources) {
      EntryExitPoint eep = EntryExitPoint.load(source, navaids, airportMainNavaid, mrvas);
      tmp.add(eep);
    }

    EntryExitPointList ret = new EntryExitPointList(tmp);
    return ret;
  }

  public static EntryExitPoint create(Navaid navaid, Type type, Integer maxMrvaAltitude) {
    EntryExitPoint ret = new EntryExitPoint(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private static EntryExitPoint load(XElement source, NavaidList navaids,
                                     Navaid airportMainNavaid,
                                     IReadOnlyList<Border> mrvas) {
    XmlLoader.setContext(source);
    String navaidName = XmlLoader.loadString("name");
    Type type = XmlLoader.loadEnum("type", Type.class);
    Navaid navaid = navaids.get(navaidName);
    Integer maxMrvaAltitude = XmlLoader.loadInteger("maxMrvaAltitude", null);
    maxMrvaAltitude = evaluateMaxMrvaAltitude(maxMrvaAltitude, navaid, airportMainNavaid, mrvas);

    EntryExitPoint ret = new EntryExitPoint(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private static Integer evaluateMaxMrvaAltitude(Integer customMaxMrvaAltitude, Navaid navaid, Navaid airportMainNavaid,
                                                 IReadOnlyList<Border> mrvas) {
    Integer maxMrvaAltitude;
    mrvas = mrvas.where(q -> q.getType() == Border.eType.mrva);
    Tuple<Coordinate, Coordinate> line =
        new Tuple<>(
            navaid.getCoordinate(),
            airportMainNavaid.getCoordinate()
        );

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (mrva.hasIntersectionWithLine(line))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }

    if (customMaxMrvaAltitude == null)
      maxMrvaAltitude = maxMrvaAlt;
    else
      maxMrvaAltitude = Math.min(customMaxMrvaAltitude, maxMrvaAlt);
    return maxMrvaAltitude;
  }

  private final Navaid navaid;
  private final Type type;
  private final Integer maxMrvaAltitude;
  private int radialFromAirport;

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
