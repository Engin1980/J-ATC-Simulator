package eng.jAtcSim.newLib.area.approaches.locations;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;

public class RegionalLocation implements ILocation {

  private final IList<Coordinate> points;

  public static RegionalLocation create(IList<Coordinate> points){
    return new RegionalLocation(points);
  }

  public static RegionalLocation create(Coordinate ... points){
    IList<Coordinate> tmp = EList.of(points);
    return new RegionalLocation(tmp);
  }

  private RegionalLocation(IList<Coordinate> points) {
    EAssert.Argument.isNotNull(points, "points");
    EAssert.Argument.isTrue(points.size() >= 3, "There must be at least 3 points to make triangle region.");
    this.points = points;
  }

  public IReadOnlyList<Coordinate> getPoints() {
    return points;
  }
}
