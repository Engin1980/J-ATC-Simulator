package eng.jAtcSim.radarBase;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.CoordinateValue;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.radarBase.global.*;

import java.util.List;

class TransformationLayer {

  class InitialData {
    final Coordinate center;
    final double widthInNm;

    public InitialData(Coordinate center, double widthInNm) {
      this.center = center;
      this.widthInNm = widthInNm;
    }
  }
  private ICanvas c;
  private Coordinate topLeft;
  private Coordinate bottomRight;
  private double scale = 75 / 1000d;
  private InitialData initialData;

  TransformationLayer(ICanvas c, Coordinate center, double widthInNm) {
    this.c = c;
    this.initialData = new InitialData(center, widthInNm);
    topLeft = center;
    bottomRight = new Coordinate(
        center.getLatitude().add(1),
        center.getLongitude().add(1)
    );
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

  double getHeightInNm(){
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
  }

  void drawText(String text, Coordinate coordinate, int xShiftInPixels, int yShiftInPixels, Font font, Color color) {
    Point p = toPoint(coordinate);
    c.drawText(text, p, xShiftInPixels, yShiftInPixels, font, color);
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

  void drawCross(Coordinate coordinate, Color color, int length, int width) {
    Point p = toPoint(coordinate);
    c.drawCross(p, color, length, width);
  }

  void drawTriangleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawTriangleAround(p, distanceInPixels, color, width);
  }

//  void setCoordinates(Coordinate topLeft, Coordinate bottomRight) {
//    //TODO tady kontrola jestli jsou u sebe
//    if (topLeft.getLongitude().get() > bottomRight.getLongitude().get()) {
//      throw new ERuntimeException("Cannot set painter coordinates. Square made of " + topLeft.toString() + " and " + bottomRight.toString() + " does not define square (longitude error).");
//    }
//    if (topLeft.getLatitude().get() < bottomRight.getLatitude().get()) {
//      throw new ERuntimeException("Cannot set painter coordinates. Square made of " + topLeft.toString() + " and " + bottomRight.toString() + " does not define square (longitude error).");
//    }
//
//    this.topLeft = topLeft;
//    this.bottomRight = bottomRight;
//  }


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
    if (c.isReady() && initialData != null){
      this.scale = initialData.widthInNm / c.getWidth();

      Coordinate localTopLeft = Coordinates.getCoordinate(
          initialData.center  , 270, initialData.widthInNm / 2);
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

  public Coordinate toCoordinate(Point point) {
    Coordinate ret = toCoordinateDelta(point);
    ret = ret.negate();
    ret = topLeft.add(ret);
    return ret;
  }

  // P R I V A T E   S T U F F
  private Point toPoint(Coordinate coord) {
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
