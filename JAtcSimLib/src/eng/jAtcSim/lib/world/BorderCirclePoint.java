package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.coordinates.Coordinate;

import java.util.Objects;

public class BorderCirclePoint extends BorderPoint {
  private Coordinate coordinate;
  private double distance;

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getDistance() {
    return distance;
  }

  @Override
  public int hashCode() {
    return Objects.hash(coordinate, distance);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BorderCirclePoint that = (BorderCirclePoint) o;
    return Double.compare(that.distance, distance) == 0 &&
        Objects.equals(coordinate, that.coordinate);
  }
}
