package eng.jAtcSim.radarBase;

import eng.eSystem.events.Event;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.radarBase.global.*;
import eng.jAtcSim.radarBase.global.events.EMouseEventArg;

import java.util.List;

public interface ICanvas<T> {
  int getWidth();
  int getHeight();
  boolean isReady();

  default void drawLine (Point from, Point to, Color color, int width){
    drawLine(from.x, from.y, to.x, to.y, color, width);
  }
  void drawLine (int x1, int y1, int x2, int y2, Color color, int width);

  default void fillRectangle (Point topLeft, Size size, Color color){
    fillRectangle(topLeft.x, topLeft.y, size.width, size.height, color);
  }

  void fillRectangle (int x, int y, int width, int height, Color color);

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

  Event<ICanvas, EMouseEventArg> getMouseEvent();
  EventSimple<ICanvas> getPaintEvent();
  Event<ICanvas, Object> getKeyEvent();
  EventSimple<ICanvas> getResizedEvent();
}
