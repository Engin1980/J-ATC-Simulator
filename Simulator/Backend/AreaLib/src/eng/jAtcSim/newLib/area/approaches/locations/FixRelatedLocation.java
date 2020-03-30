package eng.jAtcSim.newLib.area.approaches.locations;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;

public class FixRelatedLocation implements ILocation {

private final Coordinate coordinate;
private final int fromRadial;
private final int toRadial;
private final double maximalDistance;

  public FixRelatedLocation(Coordinate coordinate, int fromRadial, int toRadial, double maximalDistance) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    EAssert.Argument.isTrue(maximalDistance >= 0);
    this.coordinate = coordinate;
    this.fromRadial = fromRadial;
    this.toRadial = toRadial;
    this.maximalDistance = maximalDistance;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getFromRadial() {
    return fromRadial;
  }

  public int getToRadial() {
    return toRadial;
  }

  public double getMaximalDistance() {
    return maximalDistance;
  }
}
