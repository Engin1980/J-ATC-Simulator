package eng.jAtcSim.newLib.area.approaches.locations;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;

public class FixRelatedLocation implements ILocation {

  public static FixRelatedLocation create(Coordinate coordinate, double maximalDistance) {
    return new FixRelatedLocation(coordinate, null, null, maximalDistance);
  }

  public static FixRelatedLocation create(Coordinate coordinate, Integer fromRadial, Integer toRadial, double maximalDistance) {
    return new FixRelatedLocation(coordinate, fromRadial, toRadial, maximalDistance);
  }

  private final Coordinate coordinate;
  private final Integer fromRadial;
  private final Integer toRadial;
  private final double maximalDistance;

  private FixRelatedLocation(Coordinate coordinate, Integer fromRadial, Integer toRadial, double maximalDistance) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    EAssert.Argument.isTrue(maximalDistance >= 0);
    EAssert.Argument.isTrue((fromRadial == null && toRadial == null) || (fromRadial != null && toRadial != null),
        "Both 'fromRadial' and 'toRadial' must be set, or both must be null.");
    this.coordinate = coordinate;
    this.fromRadial = fromRadial;
    this.toRadial = toRadial;
    this.maximalDistance = maximalDistance;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Integer getFromRadial() {
    return fromRadial;
  }

  public double getMaximalDistance() {
    return maximalDistance;
  }

  public Integer getToRadial() {
    return toRadial;
  }

  @Override
  public boolean isInside(Coordinate coordinate) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    double dist = Coordinates.getDistanceInNM(coordinate, this.coordinate);
    double radial = Coordinates.getBearing(this.coordinate, coordinate);
    if (maximalDistance < dist) return false;
    if (fromRadial <= toRadial)
      return NumberUtils.isBetweenOrEqual(fromRadial, radial, toRadial);
    else
      return
          NumberUtils.isBetweenOrEqual(fromRadial, radial, 360)
              || NumberUtils.isBetweenOrEqual(0, radial, toRadial);
  }
}