package eng.jAtcSim.lib.world;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;

public class InactiveRunwayThreshold {
  private String name;
  private Coordinate coordinate;
  @XmlIgnore
  private InactiveRunway parent;
  @XmlIgnore
  private double _course;
  @XmlIgnore
  private InactiveRunwayThreshold _other;

  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public InactiveRunway getParent() {
    return parent;
  }

  public void setParent(InactiveRunway parent) {
    this.parent = parent;
  }

  public double getCourse() {
    return this._course;
  }

  public InactiveRunwayThreshold getOtherThreshold() {
    return _other;
  }

  @Override
  public String toString() {
    return this.getName() + "{inactive-rwyThr}";
  }

  public void bind() {
    this._other
        = this.getParent().getThresholdA().equals(this)
        ? this.getParent().getThresholdB()
        : this.getParent().getThresholdA();
    this._course
        = Coordinates.getBearing(this.coordinate, _other.coordinate);
  }
}
