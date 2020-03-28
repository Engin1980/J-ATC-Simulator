package eng.jAtcSim.newLib.area;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class EntryExitPoint extends Parentable<Airport> {

  public enum Type {
    entry,
    exit,
    both
  }
  
  static class XmlLoader {
    public static EntryExitPoint load(XElement source, Airport airport) {
      EntryExitPoint ret = new EntryExitPoint();
      ret.setParent(airport);
      read(source, ret);
      return ret;
    }

    private static void read(XElement source, EntryExitPoint point){
      XmlLoaderUtils.setContext(source);
      String navaidName = XmlLoaderUtils.loadString("name");
      point.type = XmlLoaderUtils.loadEnum("type", Type.class);
      point.navaid = point.getParent().getParent().getNavaids().get(navaidName);
      Integer maxMrvaAltitude = XmlLoaderUtils.loadInteger("maxMrvaAltitude", null);
      point.maxMrvaAltitude = evaluateMaxMrvaAltitude(maxMrvaAltitude, point.navaid, point.getParent());
      point.radialFromAirport = (int) Coordinates.getBearing(point.getParent().getLocation(), point.navaid.getCoordinate());
    }

    private static int evaluateMaxMrvaAltitude(Integer customMaxMrvaAltitude, Navaid navaid, Airport airport) {
      int ret;
      IList<Border> mrvas = airport.getParent().getBorders().where(q -> q.getType() == Border.eType.mrva);
      Tuple<Coordinate, Coordinate> line =
          new Tuple<>(
              navaid.getCoordinate(),
              airport.getMainAirportNavaid().getCoordinate()
          );

      int maxMrvaAlt = 0;
      for (Border mrva : mrvas) {
        if (mrva.hasIntersectionWithLine(line))
          maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
      }

      if (customMaxMrvaAltitude == null)
        ret = maxMrvaAlt;
      else
        ret = Math.min(customMaxMrvaAltitude, maxMrvaAlt);
      return ret;
    }
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
