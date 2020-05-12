package eng.jAtcSim.abstractRadar;

import eng.eSystem.events.Event;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.abstractRadar.global.*;
import eng.jAtcSim.abstractRadar.global.events.EMouseEventArg;

import java.util.List;

public interface ICanvas<T> {
  int getWidth();

  int getHeight();

  boolean isReady();

  default void drawLine(Point from, Point to, Color color, int width) {
    drawLine(from.x, from.y, to.x, to.y, color, width);
  }

  void drawLine(int x1, int y1, int x2, int y2, Color color, int width);

  default void fillRectangle(Point topLeft, Size size, Color color) {
    fillRectangle(topLeft.x, topLeft.y, size.width, size.height, color);
  }

  void fillRectangle(int x, int y, int width, int height, Color color);

  default void drawPoint(Point p, Color color, int width) {
    drawPoint(p.x, p.y, color, width);
  }

  void drawPoint(int x, int y, Color color, int width);

  void drawCircleAround(Point p, int distanceInPixels, Color color, int width);

  void drawTriangleAround(Point p, int distanceInPixels, Color color, int width);

  void drawCross(Point p, Color color, int length, int width);

  void drawArc(Point p, int xRadius, int yRadius, int fromAngle, int toAngle, Color color);

  void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, Font font, Color c);

  void drawTextBlock(List<String> lines, TextBlockLocation location, Font font, Color color);

  void clear(Color backColor);

  void beforeDraw();

  void afterDraw();

  void invokeRepaint();

  T getGuiControl();

  Event<ICanvas<?>, EMouseEventArg> getMouseEvent();

  EventSimple<ICanvas<?>> getPaintEvent();

  Event<ICanvas<?>, Object> getKeyEvent();

  EventSimple<ICanvas<?>> getResizedEvent();

  Size getEstimatedTextSize(Font font, int rowsCount, int columnsCount);

  java.awt.Rectangle getStringBounds(String text, Font font);

  default void drawAltitudeRangeBoundedBetween(Point p, String minAltitudeLabel, String maxAltitudeLabel,
                                               int xShiftInPixels, int yShiftInPixels,
                                               Font font, Color color) {
    final int ALTITUDE_LINE_SEPARATION_WIDTH = 3;
    final int SEPARATION_LINE_WIDTH = 1;
    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;
    String demoString = "X";
    int textWidth = Math.max(minAltitudeLabel == null ? 0 : minAltitudeLabel.length(),
        maxAltitudeLabel == null ? 0 : maxAltitudeLabel.length());
    java.awt.Rectangle charBounds = getStringBounds(demoString, font);

    int tx;
    int ty;
    Point up;

    x -= (int) (textWidth / 2d);
    y += charBounds.height;

    tx = x;
    ty = y + charBounds.y + ALTITUDE_LINE_SEPARATION_WIDTH / 2;
    this.drawLine(tx, ty, tx + textWidth * charBounds.width, ty, color, SEPARATION_LINE_WIDTH);

    if (maxAltitudeLabel != null) {
      tx = x + charBounds.x + charBounds.width * (textWidth - maxAltitudeLabel.length());
      ty = y + charBounds.y - charBounds.height;
      up = new Point(tx, ty);
      this.drawText(maxAltitudeLabel, up, 0, 0, font, color);
    }
    if (minAltitudeLabel != null) {
      tx = x + charBounds.x + charBounds.width * (textWidth - minAltitudeLabel.length());
      ty = y + charBounds.y + ALTITUDE_LINE_SEPARATION_WIDTH;
      up = new Point(tx, ty);
      this.drawText(minAltitudeLabel, up, 0, 0, font, color);
    }
  }

  default void drawAltitudeRangeBoundedAboveAndBelow(Point p,
                                                    String minAltitudeLabel, String maxAltitudeLabel,
                                                    int xShiftInPixels, int yShiftInPixels, Font font, Color color) {
    final int ALTITUDE_LINE_SEPARATION_WIDTH = 3;
    final int SEPARATION_LINE_WIDTH = 1;
    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;
    String demoString = minAltitudeLabel == null ? maxAltitudeLabel : minAltitudeLabel;
    java.awt.Rectangle bounds = getStringBounds(demoString, font);

    int tx;
    int ty;
    Point up;

    if (maxAltitudeLabel != null) {
      tx = x + bounds.x - bounds.width / 2;
      ty = y + bounds.y;
      up = new Point(tx,ty);
      this.drawText(maxAltitudeLabel, up, 0,0,font,color);

      ty = ty - bounds.height + ALTITUDE_LINE_SEPARATION_WIDTH;
      this.drawLine(tx, ty, tx + bounds.width, ty, color, SEPARATION_LINE_WIDTH);
    }
    if (minAltitudeLabel != null) {
      tx = x + bounds.x - bounds.width / 2;
      ty = y + bounds.y + bounds.height;
      up = new Point(tx,ty);
      this.drawText(minAltitudeLabel, up, 0,0,font,color);

      ty = ty + ALTITUDE_LINE_SEPARATION_WIDTH;
      this.drawLine(tx, ty, tx + bounds.width, ty, color, SEPARATION_LINE_WIDTH);
    }
  }

}
