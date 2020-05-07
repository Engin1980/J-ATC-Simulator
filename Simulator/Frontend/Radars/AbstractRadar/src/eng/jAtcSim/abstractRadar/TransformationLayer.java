package eng.jAtcSim.abstractRadar;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.abstractRadar.global.*;
import eng.jAtcSim.abstractRadar.global.Color;
import eng.jAtcSim.abstractRadar.global.Font;
import eng.jAtcSim.abstractRadar.global.Point;
import eng.jAtcSim.abstractRadar.global.Rectangle;

import java.util.List;

class TransformationLayer {

  private static class Rotator {

    private static int[][] poss = {{1, 0}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1},
        {-1, 2}, {0, 2}, {1, 2}, {2, 2}, {2, 1}, {2, 0}, {2, -1}, {2, -2}, {1, -2}, {0, -2},
        {-1, -2}, {-2, -2}, {-2, -1}, {-2, 0}, {-2, 1}, {-2, 2}};
    private int width;
    private int height;
    private int index;

    public Rotator(int width, int height) {
      this.width = width;
      this.height = height;
      this.index = -1;
    }

    public boolean hasNext() {
      index++;
      boolean ret = index < poss.length;
      return ret;
    }

    public Point convert(Point p) {
      int[] c = poss[index];
      Point ret = new Point(p.x + c[0] * width, p.y + c[1] * height);
      return ret;
    }
  }

  class LabelManager {
    private static final double MINIMAL_DISTANCE_TO_DRAW_CONNECTOR = 8;
    private final IList<Rectangle> rectangles = new EList<>();
    private final IList<Point> points = new EList<>();
    private int width = 100;
    private int height = 40;

    public Point tryGetSuggestedSecondPointOfConnector(Point planePoint, Point rectangleTopLeftPoint) {
      Rectangle r = getRectangleFromPoint(rectangleTopLeftPoint);
      if (r.isInside(planePoint))
        return null;

      double d = Math.min(
          Point.getManhattanDistance(planePoint, r.a),
          Point.getManhattanDistance(planePoint, r.d));

      if (d < MINIMAL_DISTANCE_TO_DRAW_CONNECTOR)
        return null;

      int x;
      int y;

      if (planePoint.x <= r.a.x)
        x = r.a.x;
      else if (planePoint.x > r.d.x)
        x = r.d.x;
      else
        x = (r.d.x + r.a.x) / 2;

      if (planePoint.y <= r.a.y)
        y = r.a.y;
      else if (planePoint.y > r.d.y)
        y = r.d.y;
      else
        y = (r.d.y + r.a.y) / 2;

      Point ret = new Point(x, y);
      return ret;

    }

    void reset() {
      rectangles.clear();
      points.clear();
    }

    void adjustDetectionRegion(int width, int height) {
      this.width = width;
      this.height = height;
    }

    Point getAdjustedPosition(Point p, Point pixelShift) {
      Point ret;

      ret = Point.sum(p, pixelShift);
      Rectangle r = getRectangleFromPoint(p);
      Point collidingPoint = tryGetCollidingPoint(r);
      Rectangle collidingRectangle = tryGetCollidingRectangle(r);

      if (collidingPoint != null || collidingRectangle != null) {
        Point tmp = tryGetNonCollidingPosition(r);
        if (tmp != null) {
          ret = tmp;
          r = getRectangleFromPoint(tmp);
        }
      }
      rectangles.add(r);

      return ret;
    }

    void addPoint(Point p) {
      this.points.add(p);
    }

    private Point tryGetNonCollidingPosition(Rectangle r) {
      Point ret = null;
      Rotator rotator = new Rotator(width, height);
      Point tmp;
      Rectangle tmr;
      while (rotator.hasNext()) {
        tmp = rotator.convert(r.a);
        tmr = getRectangleFromPoint(tmp);
        if (tryGetCollidingPoint(tmr) == null && tryGetCollidingRectangle(tmr) == null) {
          ret = tmp;
          break;
        }
      }

      return ret;
    }

    private Rectangle tryGetCollidingRectangle(Rectangle tmr) {
      Rectangle ret = rectangles.tryGetFirst(q -> tmr.hasUnion(q));
      return ret;
    }

    private Rectangle getRectangleFromPoint(Point p) {
      Rectangle ret = new Rectangle(p, new Point(p.x + width, p.y + height));
      return ret;
    }

    private Point tryGetCollidingPoint(Rectangle r) {
      Point ret = points.tryGetFirst(q -> r.isInside(q));
      return ret;
    }
  }

  class InitialData {
    final Coordinate center;
    final double widthInNm;

    public InitialData(Coordinate center, double widthInNm) {
      this.center = center;
      this.widthInNm = widthInNm;
    }
  }

  private boolean isMeReady = false;
  private ICanvas c;
  private Coordinate topLeft;
  private Coordinate bottomRight;
  private double scale = 75 / 1000d;
  private InitialData initialData;
  private LabelManager labelManager = this.new LabelManager();

  TransformationLayer(ICanvas c, Coordinate center, double widthInNm) {
    this.c = c;
    this.initialData = new InitialData(center, widthInNm);
    topLeft = center;
    bottomRight = new Coordinate(
        center.getLatitude().add(1),
        center.getLongitude().add(1)
    );
    resetPosition();
  }

  public Coordinate toCoordinate(Point point) {
    Coordinate ret = toCoordinateDelta(point);
    ret = ret.negate();
    ret = topLeft.add(ret);
    return ret;
  }

  public Point toPoint(Coordinate coord) {
    double mw = bottomRight.getLongitude().get() - topLeft.getLongitude().get();
    double mh = bottomRight.getLatitude().get() - topLeft.getLatitude().get();

    double pw = coord.getLongitude().get() - topLeft.getLongitude().get();
    double ph = coord.getLatitude().get() - topLeft.getLatitude().get();

    ph = ph / mh;
    pw = pw / mw;

    double resHeight = ph * c.getHeight();
    double resWidth = pw * c.getWidth();

    return new Point((int) resWidth, (int) resHeight);
  }

  public void adjustPlaneLabelOverlying(int width, int height) {
    this.labelManager.adjustDetectionRegion(width, height);
  }

  public void registerPlanePoint(Coordinate coordinate) {
    Point p = toPoint(coordinate);
    labelManager.addPoint(p);
  }

  public Coordinate getMiddle() {
    Coordinate ret = new Coordinate(
        (topLeft.getLatitude().getTotalDegrees() + bottomRight.getLatitude().getTotalDegrees()) / 2d,
        (topLeft.getLongitude().getTotalDegrees() + bottomRight.getLongitude().getTotalDegrees()) / 2d
         );
    return ret;
  }


  Coordinate getTopLeft() {
    return topLeft;
  }

  Coordinate getBottomRight() {
    return bottomRight;
  }

  double getWidthInNM() {
    return c.getWidth() * scale;
  }

  double getHeightInNm() {
    return c.getHeight() * scale;
  }

  void drawLine(Coordinate from, Coordinate to, Color color, int width) {
    Point f = toPoint(from);
    Point t = toPoint(to);

    c.drawLine(f, t, color, width);
  }

  void drawLineByHeadingAndDistance(Coordinate from, int heading, double lengthInNM, Color color, int width) {
    Coordinate to = Coordinates.getCoordinate(from, heading, lengthInNM);
    drawLine(from, to, color, width);
  }

  void drawPoint(Coordinate coordinate, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawPoint(p, color, width);
  }

  void drawCircleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);

    c.drawCircleAround(p, distanceInPixels, color, width);
  }

  void drawCircleAroundInNM(Coordinate coordinate, double distanceInNM, Color color, int width) {
    Size s = this.toDistance(distanceInNM);
    drawCircleAround(coordinate, s.width, color, width);
  }

  void clear(Color backColor) {
    c.clear(backColor);
    this.labelManager.reset();
  }

  void drawText(String text, Coordinate coordinate, int xShiftInPixels, int yShiftInPixels, Font font, Color color) {
    Point p = toPoint(coordinate);
    c.drawText(text, p, xShiftInPixels, yShiftInPixels, font, color);
  }

  public void drawAltitudeRangeText(Coordinate coordinate,
                                    String minAltitudeLabel, String maxAltitudeLabel,
                                    int xShiftInPixels, int yShiftInPixels,
                                    Font font, Color color) {
    Point p = toPoint(coordinate);
    c.drawAltitudeRangeBoundedBetween(p, minAltitudeLabel, maxAltitudeLabel, xShiftInPixels, yShiftInPixels, font, color);
  }


  void drawPlaneLabel(String text, boolean isFixed, Coordinate coordinate, Point pixelShift, Font font, Color color, Color connectorColor) {
    Point op = toPoint(coordinate);
    Point rp = op.clone();

    if (!isFixed)
      rp = labelManager.getAdjustedPosition(op, pixelShift);

    c.drawText(text, rp, pixelShift.x, pixelShift.y, font, color);

    Point sp = labelManager.tryGetSuggestedSecondPointOfConnector(op, rp);
    if (sp != null)
      c.drawLine(op, sp, connectorColor, 1);
  }

  void drawPlanePoint(Coordinate coordinate, Color color, int width) {
    Point p = toPoint(coordinate);
    labelManager.addPoint(p);
    c.drawPoint(p, color, width);
  }

  void drawArc(Coordinate coordinate, double fromAngle, double toAngle, double radiusInNM, Color color) {
    Point p = toPoint(coordinate);

    fromAngle = Math.round(fromAngle);
    toAngle = Math.round(toAngle);

    Size sz = toDistance(radiusInNM);

    c.drawArc(p, sz.width, sz.height, (int) fromAngle, (int) toAngle, color);
  }

  void drawLine(Coordinate coordinate, int lengthInPixels, int heading, Color color, int width) {

    Point p = toPoint(coordinate);

    heading = heading - 90;

    double x2 = p.x + Math.cos(Math.toRadians(heading)) * lengthInPixels;
    double y2 = p.y + Math.sin(Math.toRadians(heading)) * lengthInPixels;

    c.drawLine(p, new Point((int) x2, (int) y2), color, width);
  }

  void drawTextBlock(List<String> lines, TextBlockLocation location, Font font, Color color) {
    c.drawTextBlock(lines, location, font, color);
  }

  public boolean isReady(){
    return isMeReady && c.isReady();
  }

  void drawCross(Coordinate coordinate, Color color, int length, int width) {
    Point p = toPoint(coordinate);
    c.drawCross(p, color, length, width);
  }

  void drawTriangleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawTriangleAround(p, distanceInPixels, color, width);
  }

  RadarViewPort getViewPort() {
    RadarViewPort ret = new RadarViewPort(this.topLeft, this.scale * c.getWidth());
    return ret;
  }

  void setViewPort(RadarViewPort viewPort) {
    setPosition(viewPort.getTopLeft(), viewPort.getWidthInNm());
  }

  final void setPosition(Coordinate topLeft, double widthInNM) {
    if (widthInNM <= 0) {
      throw new IllegalArgumentException("Value of {widthInNM} must be greater than zero.");
    }
    this.scale = widthInNM / c.getWidth();
    setPosition(topLeft);
  }

  final void setPosition(Coordinate topLeft) {
    if (topLeft == null) {
      throw new IllegalArgumentException("Value of {topLeft} cannot not be null.");
    }

    this.topLeft = topLeft;
    resetBottomRight();
  }

  void resetPosition() {
    resetBottomRight();
  }

  private void resetBottomRight() {
    //if (c.isReady() == false) return;
    if (c.isReady() && initialData != null) {
      this.isMeReady = true;
      this.scale = initialData.widthInNm / c.getWidth();

      Coordinate localTopLeft = Coordinates.getCoordinate(
          initialData.center, 270, initialData.widthInNm / 2);
      localTopLeft = Coordinates.getCoordinate(
          localTopLeft, 0, this.getHeightInNm() / 2);
      this.topLeft = localTopLeft;
      this.initialData = null;
    }

    double widthInNm = c.getWidth() * scale;
    Coordinate tmp = Coordinates.getCoordinate(topLeft, 90, widthInNm);
    double realRatio = c.getHeight() / (double) c.getWidth();
    double heightInNM = realRatio * widthInNm;
    tmp = Coordinates.getCoordinate(tmp, 180, heightInNM);

    this.bottomRight = tmp;
  }

  Coordinate toCoordinateDelta(Point point) {
    double mw = bottomRight.getLongitude().get() - topLeft.getLongitude().get();
    double mh = bottomRight.getLatitude().get() - topLeft.getLatitude().get();

    double latD
        = point.y / (double) c.getHeight() * mh;
    double lonD
        = point.x / (double) c.getWidth() * mw;
    Coordinate ret = new Coordinate(-latD, -lonD);
    return ret;
  }

  private Size toDistance(double distanceInNM) {
    Coordinate a = topLeft.clone();
    Coordinate b;
    b = Coordinates.getCoordinate(a, 90, distanceInNM);
    double w = a.getLongitude().get() - b.getLongitude().get();
    b = Coordinates.getCoordinate(a, 180, distanceInNM);
    double h = a.getLatitude().get() - b.getLatitude().get();

    double mw = bottomRight.getLongitude().get() - topLeft.getLongitude().get();
    double mh = bottomRight.getLatitude().get() - topLeft.getLatitude().get();

    w = c.getWidth() / mw * w;
    h = c.getHeight() / mh * h;

    w = Math.abs(w);
    h = Math.abs(h);

    Size ret = new Size((int) w, (int) h);

    return ret;
  }
}
