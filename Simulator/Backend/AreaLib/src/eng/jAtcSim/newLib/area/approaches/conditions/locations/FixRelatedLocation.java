package eng.jAtcSim.newLib.area.approaches.conditions.locations;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;

import exml.annotations.XConstructor;

public class FixRelatedLocation implements ILocation {

  public static FixRelatedLocation create(Coordinate coordinate, double maximalDistance) {
    return new FixRelatedLocation(coordinate, null, null, maximalDistance);
  }

  public static FixRelatedLocation create(Coordinate coordinate, Integer fromRadial, Integer toRadial, double maximalDistance) {
    return new FixRelatedLocation(coordinate, fromRadial, toRadial, maximalDistance);
  }

  public static FixRelatedLocation create(Coordinate coordinate, int fromRadial, int toRadial){
    return new FixRelatedLocation(coordinate, fromRadial, toRadial, Double.MAX_VALUE);
  }

  private final Coordinate coordinate;
  private final Integer fromRadial;
  private final Integer toRadial;
  private final double maximalDistance;

  @XConstructor
  private FixRelatedLocation() {
    coordinate = null;
    fromRadial = null;
    toRadial = null;
    maximalDistance = -1;

    PostContracts.register(this, () -> coordinate != null);
    PostContracts.register(this, () -> maximalDistance != -1);
  }

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
    double dist = Coordinates.getDistanceInNM(this.coordinate, coordinate);
    double radial = Coordinates.getBearing(this.coordinate,coordinate);

    if (maximalDistance < dist) return false;
    if (fromRadial == null) // toRadial is null too
      return true;
    else
      return Headings.isBetween(fromRadial, radial, toRadial);
  }
}
