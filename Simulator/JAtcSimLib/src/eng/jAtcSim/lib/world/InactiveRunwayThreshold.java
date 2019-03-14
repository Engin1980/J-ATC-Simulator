package eng.jAtcSim.lib.world;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Coordinate;

public class InactiveRunwayThreshold {
  public static InactiveRunwayThreshold[] create(String aName, Coordinate aCoordinate, String bName, Coordinate bCoordinate,
                                                 InactiveRunway parent) {
    InactiveRunwayThreshold a = new InactiveRunwayThreshold(aName, aCoordinate, parent);
    InactiveRunwayThreshold b = new InactiveRunwayThreshold(bName, bCoordinate, parent);
    a.other = b;
    b.other = a;
    a.course = Coordinates.getBearing(a.coordinate, b.coordinate);
    b.course = Coordinates.getBearing(b.coordinate, a.coordinate);

    return new InactiveRunwayThreshold[]{a, b};
  }

  private final String name;
  private final Coordinate coordinate;
  private final InactiveRunway parent;
  private double course;
  private InactiveRunwayThreshold other;

  public InactiveRunwayThreshold(String name, Coordinate coordinate, InactiveRunway parent) {
    this.name = name;
    this.coordinate = coordinate;
    this.parent = parent;
  }


  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public InactiveRunway getParent() {
    return parent;
  }

  public double getCourse() {
    return this.course;
  }

  public InactiveRunwayThreshold getOtherThreshold() {
    return other;
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwyThr}";
  }

  public void bind() {
    this.other
        = this.getParent().getThresholdA().equals(this)
        ? this.getParent().getThresholdB()
        : this.getParent().getThresholdA();
    this.course
        = Coordinates.getBearing(this.coordinate, other.coordinate);
  }
}
