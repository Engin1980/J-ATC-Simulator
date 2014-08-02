/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimdraw.canvases.Canvas;
import jatcsimlib.providers.Coordinates;
import jatcsimlib.types.Coordinate;
import java.awt.Color;

/**
 *
 * @author Marek
 */
public class BasicPainter extends Painter {

  public BasicPainter(Canvas c, Coordinate topLeft, Coordinate bottomRight) {
    super(c, topLeft, bottomRight);
  }

  @Override
  void drawLine(Coordinate from, Coordinate to, Color color, int width) {
    Point f = toPoint(from);
    Point t = toPoint(to);

    c.drawLine(f, t, color, width);
  }

  @Override
  void drawPoint(Coordinate coordinate, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawPoint(p, color, width);
  }

  @Override
  void drawCircleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);

    c.drawCircleAround(p, distanceInPixels, color, width);
  }

  @Override
  void clear(Color backColor) {
    c.clear(backColor);
  }

  @Override
  void drawText(String text, Coordinate coordinate, int xShiftInPixels, int yShiftInPixels, Color color) {
    Point p = toPoint(coordinate);
    c.drawText(text, p, xShiftInPixels, yShiftInPixels, color);
  }

  @Override
  void drawTriangleAround(Coordinate coordinate, int distanceInPixels, Color color, int width) {
    Point p = toPoint(coordinate);
    c.drawTriangleAround(p, distanceInPixels, color, width);
  }

  @Override
  void drawArc(Coordinate coordinate, double fromAngle, double toAngle, double radiusInNM, Color color) {
    Point p = toPoint(coordinate);

    fromAngle = Math.round(fromAngle);
    toAngle = Math.round(toAngle);

    Size sz = toDistance(radiusInNM);

    c.drawArc(p, sz.width, sz.height, (int) fromAngle, (int) toAngle, color);
  }
}
