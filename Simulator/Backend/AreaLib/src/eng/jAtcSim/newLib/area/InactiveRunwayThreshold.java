package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class InactiveRunwayThreshold extends Parentable<InactiveRunway> {

  public static class Prototype{
    public String name;
    public Coordinate coordinate;

    public Prototype(String name, Coordinate coordinate) {
      this.name = name;
      this.coordinate = coordinate;
    }
  }

  public static IList<InactiveRunwayThreshold> create(
      Prototype firstThreshold,
      Prototype secondThreshold
  ){
    EAssert.Argument.isNotNull(firstThreshold, "Parameter 'firstThreshold' cannot be null.");
    EAssert.Argument.isNotNull(secondThreshold, "Parameter 'secondThreshold' cannot be null.");

    InactiveRunwayThreshold a = new InactiveRunwayThreshold(firstThreshold.name, firstThreshold.coordinate);
    InactiveRunwayThreshold b = new InactiveRunwayThreshold(secondThreshold.name, secondThreshold.coordinate);

    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    IList<InactiveRunwayThreshold> ret = new EList<>();
    ret.add(a);
    ret.add(b);
    return ret;
  }

  private final String name;
  private final Coordinate coordinate;
  private double course;
  private InactiveRunwayThreshold other;

  private InactiveRunwayThreshold(String name, Coordinate coordinate) {
    EAssert.Argument.isNotNull(name, "Parameter 'name' cannot be null.");
    EAssert.Argument.isNotNull(coordinate, "Parameter 'coordinate' cannot be null.");
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
