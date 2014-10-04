/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcismdraw.radarBase;

import jatcsimdraw.global.Point;
import jatcsimdraw.global.Size;
import jatcismdraw.radarBase.Canvas;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.coordinates.Coordinate;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author Marek
 */
public abstract class Painter {

  public enum eTextBlockLocation {

    topLeft,
    topMiddle,
    topRight,
    middleLeft,
    middleRight,
    bottomLeft,
    bottomMiddle,
    bottomRight
  }

  public enum eTextType {

    navaid,
    plane,
    message
  }

  protected Canvas c;
  protected Coordinate topLeft;
  protected Coordinate bottomRight;

  public Coordinate getTopLeft() {
    return topLeft;
  }

  public Coordinate getBottomRight() {
    return bottomRight;
  }

  public final void setCoordinates(Coordinate topLeft, Coordinate bottomRight) {
    //TODO tady kontrola jestli jsou u sebe
    if (topLeft.getLongitude().get() > bottomRight.getLongitude().get()) {
      throw new ERuntimeException("Cannot set painter coordinates. Square made of " + topLeft.toString() + " and " + bottomRight.toString() + " does not define square (longitude error).");
    }
    if (topLeft.getLatitude().get() < bottomRight.getLatitude().get()) {
      throw new ERuntimeException("Cannot set painter coordinates. Square made of " + topLeft.toString() + " and " + bottomRight.toString() + " does not define square (longitude error).");
    }

    this.topLeft = topLeft;
    this.bottomRight = bottomRight;
  }

  public Painter(Canvas c, Coordinate topLeft, Coordinate bottomRight) {
    this.c = c;
    setCoordinates(topLeft, bottomRight);
  }

  /**
   * Convert lat-lon coordinate into x-y pixel coordinate.
   * @param coord Lat-lon coordinate
   * @return x-y pixel coordinate
   */
  protected Point toPoint(Coordinate coord) {
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

  protected Size toDistance(double distanceInNM) {
    Coordinate a = topLeft.clone(); //new Coordinate(0, 0);
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

  public Coordinate toCoordinateDelta(Point point) {
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

  protected abstract void drawLine(Coordinate from, Coordinate to, Color color, int width);

  protected void drawLine(Coordinate from, Coordinate to, Color color) {
    drawLine(from, to, color, 1);
  }

  protected void drawLineByHeadingAndDistance(Coordinate from, int heading, double lengthInNM, Color color, int width) {
    Coordinate to = Coordinates.getCoordinate(from, heading, lengthInNM);
    
    drawLine(from, to, color, width);
  }

  protected abstract void drawPoint(Coordinate coordinate, Color color, int width);

  protected abstract void drawCircleAround(Coordinate coordinate, int radiusInPixels, Color color, int width);

  protected void drawCircleAroundInNM(Coordinate coordinate, double distanceInNM, Color color, int width) {
    Size s = this.toDistance(distanceInNM);

    drawCircleAround(coordinate, s.width, color, width);
  }

  protected abstract void drawTriangleAround(Coordinate coordinate, int distanceInPixels, Color color, int width);

  protected abstract void drawArc(Coordinate coordinate, double fromAngle, double toAngle, double radiusInNM, Color color);

  protected abstract void clear(Color backColor);

  protected abstract void drawText(String name, Coordinate coordinate, int xShiftInPixels, int yShiftInPixels, Color color, eTextType textType);

  protected abstract void drawLine(Coordinate coordinate, int lengthInPixels, int heading, Color color, int width);

  protected abstract void drawTextBlock(List<String> lines, eTextBlockLocation location, Color color, eTextType textType);
  
  public void beforeDraw(){
    c.beforeDraw();
  }
  public void afterDraw(){
    c.afterDraw();
  }
}
