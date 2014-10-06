/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcismdraw.radarBase;

import jatcsimdraw.global.Point;
import jatcsimdraw.global.Size;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimdraw.shared.es.EMouseEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 *
 * @author Marek
 */
public abstract class Canvas {
  public abstract int getWidth();
  public abstract int getHeight();
  
  public void drawLine (Point from, Point to, Color color, int width){
    drawLine(from.x, from.y, to.x, to.y, color, width);
  }
  public abstract void drawLine (int x1, int y1, int x2, int y2, Color color, int width);
  
  public void fillRectangle (Point topLeft, Size size, Color color){
    fillRectangle(topLeft.x, topLeft.y, size.width, size.height, color);
  }
  public abstract void fillRectangle (int x, int y, int width, int height, Color color);

  public void drawPoint(Point p, Color color, int width) {
    drawPoint(p.x, p.y, color, width);
  }
  public abstract void drawPoint(int x, int y, Color color, int width);

  public abstract void drawCircleAround(Point p, int distanceInPixels, Color color, int width);
  
  public abstract void drawTriangleAround(Point p, int distanceInPixels, Color color, int width);

  public abstract void drawArc(Point p, int xRadius, int yRadius, int fromAngle, int toAngle, Color color);
  
  public abstract void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, Font font, Color c);
  
  public abstract void drawTextBlock(List<String> lines, Painter.eTextBlockLocation location, Font font, Color color);
  
  public abstract void clear(Color backColor);
  
  public void beforeDraw(){}
  public void afterDraw(){}
  
  /**
   * Forces canvas to invoke "paint" event.
   */
  public abstract void repaint();
  
  public abstract EventManager<Canvas, EventListener<Canvas, EMouseEvent>, EMouseEvent> onMouseEvent();
  public abstract EventManager<Canvas, EventListener<Canvas, Object>, Object> onPaint();
  public abstract EventManager<Canvas, EventListener<Canvas, KeyEvent>, KeyEvent> onKeyPress();  
}
