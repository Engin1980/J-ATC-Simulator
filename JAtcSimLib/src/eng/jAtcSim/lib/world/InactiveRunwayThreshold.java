package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.KeyItem;

public class InactiveRunwayThreshold implements KeyItem<String> {
  private String name;
  private Coordinate coordinate;
  private InactiveRunway parent;
  private double _course;
  private InactiveRunwayThreshold _other;

  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override
  public String getKey() {
    return getName();
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
