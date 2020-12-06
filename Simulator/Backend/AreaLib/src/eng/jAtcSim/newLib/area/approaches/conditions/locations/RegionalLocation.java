package eng.jAtcSim.newLib.area.approaches.conditions.locations;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geometry2D.Point;
import eng.eSystem.geometry2D.Polygon;
import eng.eSystem.validation.EAssert;

public class RegionalLocation implements ILocation {

  public static RegionalLocation create(IList<Coordinate> points) {
    return new RegionalLocation(points);
  }

  public static RegionalLocation create(Coordinate... points) {
    IList<Coordinate> tmp = EList.of(points);
    return new RegionalLocation(tmp);
  }
  private final IList<Coordinate> points;
  private Polygon polygon = null;

  private RegionalLocation(IList<Coordinate> points) {
    EAssert.Argument.isNotNull(points, "points");
    EAssert.Argument.isTrue(points.size() >= 3, "There must be at least 3 points to make triangle region.");
    this.points = points;
  }

  public IReadOnlyList<Coordinate> getPoints() {
    return points;
  }

  @Override
  public boolean isInside(Coordinate coordinate) {
    if (polygon == null)
      polygon = new Polygon(points.select(q -> new Point(q.getLatitude().get(), q.getLongitude().get())));
    Point point = new Point(coordinate.getLatitude().get(), coordinate.getLongitude().get());
    boolean ret = polygon.isInside(point);
    return ret;
  }
}
