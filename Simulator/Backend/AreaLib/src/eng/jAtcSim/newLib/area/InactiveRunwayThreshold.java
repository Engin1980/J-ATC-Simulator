package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class InactiveRunwayThreshold extends Parentable<InactiveRunway> {

  public static IList<InactiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources, InactiveRunway parent) {
    assert sources.size() == 2 : "There must be two thresholds";

    InactiveRunwayThreshold a = InactiveRunwayThreshold.load(sources.get(0), parent);
    InactiveRunwayThreshold b = InactiveRunwayThreshold.load(sources.get(1), parent);
    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    IList<InactiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private static InactiveRunwayThreshold load(XElement source, InactiveRunway parent) {
    InactiveRunwayThreshold ret = new InactiveRunwayThreshold();
    ret.setParent(parent);
    ret.read(source);
    return ret;
  }

  private void read(XElement source) {
    XmlLoader.setContext(source);
    this.name = XmlLoader.loadString("name");
    this. coordinate = XmlLoader.loadCoordinate("coordinate");
  }

  private String name;
  private Coordinate coordinate;
  private double course;
  private InactiveRunwayThreshold other;

  private InactiveRunwayThreshold() {
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
