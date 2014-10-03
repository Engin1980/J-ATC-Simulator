/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcismdraw.radarBase;

import jatcismdraw.radarBase.Painter;
import jatcsimdraw.global.Point;
import jatcsimdraw.global.Size;
import jatcismdraw.radarBase.Canvas;
import jatcsimlib.coordinates.Coordinate;
import java.awt.Color;
import java.util.List;

/**
 *
 * @author Marek
 */
public class BasicPainter extends Painter {

  public BasicPainter(Canvas c, Coordinate topLeft, Coordinate bottomRight) {
    super(c, topLeft, bottomRight);
  }

  @Override
  protected void drawLine(Coordinate from, Coordinate to, Color color, int width) {
    Point f = toPoint(from);
    Point t = toPoint(to);

    c.drawLine(f, t, color, width);
  }

  @Override
  protected void drawPoint(Coordinate coordinate, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawPoint(p, color, width);
  }

  @Override
  protected void drawCircleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);

    c.drawCircleAround(p, distanceInPixels, color, width);
  }

  @Override
  protected void clear(Color backColor) {
    c.clear(backColor);
  }

  @Override
  protected void drawText(String text, Coordinate coordinate, int xShiftInPixels, int yShiftInPixels, Color color, eTextType textType) {
    Point p = toPoint(coordinate);
    c.drawText(text, p, xShiftInPixels, yShiftInPixels, color, textType);
  }

  @Override
  protected void drawTriangleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawTriangleAround(p, distanceInPixels, color, width);
  }

  @Override
  protected void drawArc(Coordinate coordinate, double fromAngle, double toAngle, double radiusInNM, Color color) {
    Point p = toPoint(coordinate);

    fromAngle = Math.round(fromAngle);
    toAngle = Math.round(toAngle);

    Size sz = toDistance(radiusInNM);

    c.drawArc(p, sz.width, sz.height, (int) fromAngle, (int) toAngle, color);
  }

  @Override
  protected void drawLine(Coordinate coordinate, int lengthInPixels, int heading, Color color, int width) {

    Point p = toPoint(coordinate);

    heading = heading - 90;

    double x2 = p.x + Math.cos(Math.toRadians(heading)) * lengthInPixels;
    double y2 = p.y + Math.sin(Math.toRadians(heading)) * lengthInPixels;

    c.drawLine(p, new Point((int) x2, (int) y2), color, width);
  }

  @Override
  protected void drawTextBlock(List<String> lines, eTextBlockLocation location, Color color, eTextType textType) {
    c.drawTextBlock(lines, location, color, textType);
  }
}
