/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimdraw.canvases.Canvas;
import jatcsimdraw.shared.EventListener;
import jatcsimdraw.shared.EventManager;
import jatcsimdraw.shared.es.EMouseEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 *
 * @author Marek
 */
public class EJComponentCanvas extends Canvas {

  private final EJComponent c;
  private Graphics g;
  private final EJComponentCanvas me = this;

  public EJComponentCanvas() {
    this(new EJComponent());
  }

  public EJComponentCanvas(EJComponent c) {
    this.c = c;
    c.paintEM.addListener(new EventListener<EJComponent, Graphics>() {

      @Override
      public void raise(EJComponent parent, Graphics e) {
        me.g = e;
        paintEM.raise(null);
      }
    });
    c.addMouseListener(new MouseAdapter() {

      private java.awt.Point dragStartPoint = null;
      private int MINIMUM_DRAG_SHIFT = 3;

      @Override
      public void mouseClicked(MouseEvent e) {
        EMouseEvent eme;
        if (e.getClickCount() == 2) {
          eme = new EMouseEvent(e.getPoint(), EMouseEvent.eType.DoubleClick);
        } else {
          eme = new EMouseEvent(e.getPoint(), EMouseEvent.eType.Click);
        }
        mouseEventEM.raise(eme);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        dragStartPoint = e.getPoint();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        java.awt.Point dragEndPoint = e.getPoint();
        Point p = new Point(
            dragEndPoint.x - dragStartPoint.x,
            dragEndPoint.y - dragStartPoint.y);
        dragStartPoint = null;
        if (p.x < MINIMUM_DRAG_SHIFT && p.y < MINIMUM_DRAG_SHIFT) {
          return;
        }

        EMouseEvent eme = new EMouseEvent(
            p.x, p.y, EMouseEvent.eType.Drag);
        me.mouseEventEM.raise(eme);
      }
    });
    c.addMouseMotionListener(new MouseAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        EMouseEvent eme = new EMouseEvent(e.getPoint(), EMouseEvent.eType.Move);
        mouseEventEM.raise(eme);
      }
    });
    c.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        me.keyPressEM.raise(e);
      }
    });
    c.addMouseWheelListener(new MouseWheelListener() {

      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        EMouseEvent eme = new EMouseEvent(
            e.getPoint(), e.getWheelRotation(), EMouseEvent.eType.WheelScroll);
        mouseEventEM.raise(eme);
      }
    });
  }

  public EJComponent getEJComponent() {
    return c;
  }

  @Override
  public int getWidth() {
    return g.getClipBounds().width; // c.getWidth();
  }

  @Override
  public int getHeight() {
    return g.getClipBounds().height; //c.getHeight();
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2, Color color, int width) {
    g.setColor(color);
    g.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void fillRectangle(int x, int y, int width, int height, Color color) {
    g.setColor(color);
    g.fillRect(x, y, width, height);
  }

  @Override
  public void repaint() {
    c.repaint();
  }

  @Override
  public void clear(Color backColor) {
    int h = getHeight();
    int w = getWidth();
    g.fillRect(0, 0, w, h);
  }

  @Override
  public void drawPoint(int x, int y, Color color, int width) {
    g.setColor(color);
    int step = width / 2;
    g.fillOval(x - step, y - step, width, width);
  }

  @Override
  public void drawCircleAround(Point p, int distanceInPixels, Color color, int width) {
    g.setColor(color);
    int step = distanceInPixels / 2;
    g.drawOval(p.x - step, p.y - step, distanceInPixels, distanceInPixels);
  }

  @Override
  public void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, Color c) {
    FontMetrics fm = g.getFontMetrics();
    Rectangle b = fm.getStringBounds(text, g).getBounds();

    g.drawString(text, p.x + xShiftInPixels, p.y + b.height + yShiftInPixels);
  }

  @Override
  public void setFont(String name, int size) {
    Font f = new Font(name, 0, size);
    this.c.setFont(f);
  }

  @Override
  public void drawTriangleAround(Point p, int distanceInPixels, Color color, int width) {
    Point[] pts = new Point[3];
    double tStep = distanceInPixels / 3d;
    pts[0] = new Point(p.x, p.y - (int) (2 * tStep));

    double xStep = Math.sqrt((2 * tStep) * (2 * tStep) - tStep * tStep);
    pts[1] = new Point(p.x - (int) xStep, p.y + (int) tStep);
    pts[2] = new Point(p.x + (int) xStep, p.y + (int) tStep);

    drawLine(pts[0], pts[1], color, width);
    drawLine(pts[1], pts[2], color, width);
    drawLine(pts[2], pts[0], color, width);
  }

  private final EventManager<Canvas, EventListener<Canvas, EMouseEvent>, EMouseEvent> mouseEventEM = new EventManager(this);

  @Override
  public EventManager<Canvas, EventListener<Canvas, EMouseEvent>, EMouseEvent> onMouseEvent() {
    return this.mouseEventEM;
  }

  private final EventManager paintEM = new EventManager(this);

  @Override
  public EventManager<Canvas, EventListener<Canvas, Object>, Object> onPaint() {
    return this.paintEM;
  }

  private final EventManager<Canvas, EventListener<Canvas, KeyEvent>, KeyEvent> keyPressEM = new EventManager(this);

  @Override
  public EventManager<Canvas, EventListener<Canvas, KeyEvent>, KeyEvent> onKeyPress() {
    return this.keyPressEM;
  }

  @Override
  public void drawArc(Point p, int xRadius, int yRadius, int fromAngle, int toAngle, Color color) {
    g.setColor(color);
    Point orig = new Point(p.x - xRadius, p.y - yRadius);
    int angleLength = (toAngle < fromAngle) ? (toAngle + 360) : toAngle - fromAngle;
    fromAngle = toEJComponentAngle(fromAngle);
    g.drawArc(orig.x, orig.y, xRadius+xRadius, yRadius+yRadius, fromAngle, -angleLength);
  }
  
  private int toEJComponentAngle (int angle){
    return 360-angle + 90;
  }

}
