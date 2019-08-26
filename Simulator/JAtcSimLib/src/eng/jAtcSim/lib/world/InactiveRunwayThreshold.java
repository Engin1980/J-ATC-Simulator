package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.world.xml.XmlLoader;

public class InactiveRunwayThreshold extends Parentable<InactiveRunway> {

  public static IList<InactiveRunwayThreshold> loadList(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    InactiveRunwayThreshold a = InactiveRunwayThreshold.load(sources.get(0));
    InactiveRunwayThreshold b = InactiveRunwayThreshold.load(sources.get(1));
    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    IList<InactiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private static InactiveRunwayThreshold load(XElement source) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    Coordinate coordinate = XmlLoader.loadCoordinate("coordinate");

    InactiveRunwayThreshold ret = new InactiveRunwayThreshold(name, coordinate);
    return ret;
  }

  private final String name;
  private final Coordinate coordinate;
  private double course;
  private InactiveRunwayThreshold other;

  private InactiveRunwayThreshold(String name, Coordinate coordinate) {
    this.name = name;
    this.coordinate = coordinate;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getCourse() {
    return this.course;
  }

  public String getName() {
    return name;
  }

  public InactiveRunwayThreshold getOtherThreshold() {
    return other;
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwyThr}";
  }
}
