package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.coordinates.Coordinate;

import java.util.Objects;

public class BorderCrdPoint extends BorderPoint {
  private Coordinate coordinate;
  private int radial;
  private double distance;

  public BorderCrdPoint(Coordinate coordinate, int radial, double distance) {
    this.coordinate = coordinate;
    this.radial = radial;
    this.distance = distance;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getRadial() {
    return radial;
  }

  public double getDistance() {
    return distance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BorderCrdPoint that = (BorderCrdPoint) o;
    return radial == that.radial &&
        Double.compare(that.distance, distance) == 0 &&
        Objects.equals(coordinate, that.coordinate);
  }

  @Override
  public int hashCode() {

    return Objects.hash(coordinate, radial, distance);
  }
}
