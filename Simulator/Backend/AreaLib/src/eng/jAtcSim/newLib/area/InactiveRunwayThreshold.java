package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class InactiveRunwayThreshold extends Parentable<InactiveRunway> {

  static class XmlReader {
    static IList<InactiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources, InactiveRunway parent) {
      assert sources.size() == 2 : "There must be two thresholds";

      InactiveRunwayThreshold a = InactiveRunwayThreshold.XmlReader.load(sources.get(0), parent);
      InactiveRunwayThreshold b = InactiveRunwayThreshold.XmlReader.load(sources.get(1), parent);
      a.other = b;
      b.other = a;
      a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
      b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

      IList<InactiveRunwayThreshold> ret = new EList<>();
      ret.add(a);
      ret.add(b);
      return ret;
    }

    private static InactiveRunwayThreshold load(XElement source, InactiveRunway runway) {
      InactiveRunwayThreshold ret = new InactiveRunwayThreshold();
      ret.setParent(runway);
      read(source, ret);
      return ret;
    }

    private static void read(XElement source, InactiveRunwayThreshold inactiveRunwayThreshold) {
      XmlLoader.setContext(source);
      XmlLoader.setContext(source);
      inactiveRunwayThreshold.name = XmlLoader.loadString("name");
      inactiveRunwayThreshold.coordinate = XmlLoader.loadCoordinate("coordinate");
    }
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
