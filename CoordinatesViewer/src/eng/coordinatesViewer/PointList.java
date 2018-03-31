package eng.coordinatesViewer;

import eng.eSystem.collections.EList;

public class PointList extends EList<Point> {
  private static final double MAX_AXIS_DISTANCE = 10;


  public Point tryAlignToExisting(Point p, double zoomRatio) {
    double dist = MAX_AXIS_DISTANCE * zoomRatio;

    Point ret = this.tryGetFirst(q -> Math.abs(q.x - p.x) < dist && Math.abs(q.y - p.y) < dist);
    return ret;
  }
}
