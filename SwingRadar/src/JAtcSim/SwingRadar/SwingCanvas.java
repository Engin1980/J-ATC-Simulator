package JAtcSim.SwingRadar;

import JAtcSim.radarBase.ICanvas;
import JAtcSim.radarBase.global.Color;
import JAtcSim.radarBase.global.Font;
import JAtcSim.radarBase.global.Point;
import JAtcSim.radarBase.global.TextBlockLocation;
import JAtcSim.radarBase.global.events.EKeyboardModifier;
import JAtcSim.radarBase.global.events.EMouseEvent;
import eng.eSystem.events.Event;
import jatcsimlib.exceptions.ENotSupportedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SwingCanvas implements JAtcSim.radarBase.ICanvas<JComponent> {

  /**
   * Margin on x axis for text prints.
   */
  private static final int xMargin = 4;
  /**
   * Margin on y axis for text prints.
   */
  private static final int yMargin = -2;

  private final JComponent c;

  private Graphics g;

  private eng.eSystem.events.Event<ICanvas, EMouseEvent> mouseEvent =
      new eng.eSystem.events.Event<>(this);

  private eng.eSystem.events.Event<ICanvas, Object> paintEvent =
    new eng.eSystem.events.Event<>(this);

  private Event<ICanvas, Object> keyEvent =
      new Event<>(this);

  public SwingCanvas() {
    this.c = new JPanel(){
      @Override
      public void paint(Graphics g) {
        SwingCanvas.this.g = g;
        SwingCanvas.this.paintEvent.raise(null);
      }
    };
    this.c.setFocusable(true);

    MouseAdapter ma;
    ma = new MouseAdapter() {

      private java.awt.Point dragStartPoint = null;
      private int dragStartModifiers = 0;
      private int MINIMUM_DRAG_SHIFT = 3;

      @Override
      public void mouseClicked(MouseEvent e) {
        EMouseEvent eme;
        EMouseEvent.eType type;
        if (e.getClickCount() == 2) {
          // TODO dopsat key modifikatory alt/shift/ctr
          type = EMouseEvent.eType.DoubleClick;
        } else {
          type = EMouseEvent.eType.Click;

        }
        eme = EMouseEvent.createClick(
            e.getPoint().x, e.getPoint().y, type,
            EMouseEvent.eButton.convertFromSpringButton(e.getButton()), EKeyboardModifier.NONE);
        SwingCanvas.this.mouseEvent.raise(eme);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        dragStartPoint = e.getPoint();
        dragStartModifiers = e.getModifiers();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (dragStartPoint == null) {
          return;
        }
        java.awt.Point dragEndPoint = e.getPoint();
        Point diffPoint = new Point(
            dragEndPoint.x - dragStartPoint.x,
            dragEndPoint.y - dragStartPoint.y);
        if (diffPoint.x < MINIMUM_DRAG_SHIFT && diffPoint.y < MINIMUM_DRAG_SHIFT) {
          mouseClicked(e); // if move not enough big for drag, then it is a click
        }

        EMouseEvent eme = EMouseEvent.createDrag(
            dragStartPoint.x, dragStartPoint.y, dragEndPoint.x, dragEndPoint.y,
            EMouseEvent.eButton.convertFromSpringButton(e.getButton()),
            new EKeyboardModifier(dragStartModifiers));
        dragStartModifiers = 0;
        dragStartPoint = null;

        SwingCanvas.this.mouseEvent.raise(eme);
      }
    };
    c.addMouseListener(ma);

    ma = new MouseAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        EMouseEvent eme = EMouseEvent.createMove(e.getPoint().x, e.getPoint().y);
        SwingCanvas.this.mouseEvent.raise(eme);
      }
    };
    c.addMouseMotionListener(ma);

    KeyAdapter ka;
    ka = new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        SwingCanvas.this.keyEvent.raise(e);
      }
    };
    c.addKeyListener(ka);

    MouseWheelListener mw;
    mw = new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        EMouseEvent eme = EMouseEvent.createScroll(
            e.getPoint().x, e.getPoint().y, e.getWheelRotation());
        SwingCanvas.this.mouseEvent.raise(eme);
      }
    };
    c.addMouseWheelListener(mw);
  }

  @Override
  public int getWidth() {
    return g.getClipBounds().width;
  }

  @Override
  public int getHeight() {
    return g.getClipBounds().height;
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2, Color color, int width) {
    g.setColor(Coloring.get(color));
    g.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void fillRectangle(int x, int y, int width, int height, Color color) {
    g.setColor(Coloring.get(color));
    g.fillRect(x, y, width, height);
  }

  @Override
  public void drawPoint(int x, int y, Color color, int width) {
    g.setColor(Coloring.get(color));
    int step = width / 2;
    g.fillOval(x - step, y - step, width, width);
  }

  @Override
  public void drawCircleAround(Point p, int distanceInPixels, Color color, int width) {
    g.setColor(Coloring.get(color));
    int step = distanceInPixels / 2;
    g.drawOval(p.x - step, p.y - step, distanceInPixels, distanceInPixels);
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

  @Override
  public void drawCross(Point p, Color color, int length, int width) {
    int hl = length / 2;

    Point topLeft = new Point(p.x - hl, p.y - hl);
    Point bottomRight = new Point(p.x + hl, p.y + hl);
    drawLine(topLeft, bottomRight, color, width);

    Point topRight = new Point(p.x + hl, p.y - hl);
    Point bottomLeft = new Point(p.x - hl, p.y + hl);
    drawLine(topRight, bottomLeft, color, width);
  }

  @Override
  public void drawArc(Point p, int xRadius, int yRadius, int fromAngle, int toAngle, Color color) {
    g.setColor(Coloring.get(color));
    Point orig = new Point(p.x - xRadius, p.y - yRadius);
    int angleLength = (toAngle < fromAngle) ? (toAngle + 360) : toAngle - fromAngle;
    fromAngle = toEJComponentAngle(fromAngle);
    g.drawArc(orig.x, orig.y, xRadius + xRadius, yRadius + yRadius, fromAngle, -angleLength);
  }

  @Override
  public void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, Font font, Color c) {
    String[] lines = text.split(System.getProperty("line.separator"));

    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;

    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(c));

    for (String line : lines) {
      FontMetrics fm = g.getFontMetrics();
      Rectangle b = fm.getStringBounds(line, g).getBounds();

      y = y + b.height - 5;

      g.drawString(line, x, y);
    }
  }

  @Override
  public void drawTextBlock(List<String> lines, TextBlockLocation location, Font font, Color color) {
    if (location == JAtcSim.radarBase.global.TextBlockLocation.bottomMiddle
        || location == JAtcSim.radarBase.global.TextBlockLocation.middleLeft
        || location == JAtcSim.radarBase.global.TextBlockLocation.middleRight
        || location == JAtcSim.radarBase.global.TextBlockLocation.topMiddle) {
      //TODO remove this, move this exception to somewhere shared and remove
      // dependency on JAtcSimLib
      throw new ENotSupportedException();
    }
    if (lines.isEmpty()) {
      return;
    }

    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(color));

    Point[] pts = getPositionsForText(lines, location);
    for (int i = 0; i < lines.size(); i++) {
      g.drawString(lines.get(i), pts[i].x, pts[i].y);
    }
  }

  @Override
  public void clear(Color backColor) {
    int h = getHeight();
    int w = getWidth();
    g.fillRect(0, 0, w, h);
  }

  @Override
  public void beforeDraw() {
  }

  @Override
  public void afterDraw() {
  }

  @Override
  public JComponent getGuiControl(){
    return this.c;
  }

  @Override
  public eng.eSystem.events.Event<ICanvas, EMouseEvent> getMouseEvent() {
    return mouseEvent;
  }

  @Override
  public eng.eSystem.events.Event<ICanvas, Object> getPaintEvent() {
    return paintEvent;
  }

  @Override
  public Event<ICanvas, Object> getKeyEvent() {
    return keyEvent;
  }

  private int toEJComponentAngle(int angle) {
    return 360 - angle + 90;
  }

  private Point[] getPositionsForText(List<String> lines, JAtcSim.radarBase.global.TextBlockLocation location) {

    int lastX;
    int lastY;
    int maxX;
    Point[] ret = new Point[lines.size()];
    FontMetrics fm = g.getFontMetrics();

    switch (location) {
      case topLeft:
        lastX = xMargin;
        lastY = yMargin + 16;
        for (int i = 0; i < lines.size(); i++) {
          ret[i] = new Point(lastX, lastY);
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          lastY += r.height;
        }
        break;
      case bottomLeft:
        lastX = xMargin;
        lastY = this.getHeight() - yMargin; // g.getClipBounds().height - yMargin;
        for (int i = lines.size() - 1; i >= 0; i--) {
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          lastY -= r.height;
          ret[i] = new Point(lastX, lastY);
        }
        break;
      case topRight:
        lastY = yMargin + 16;
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width - xMargin;
        for (int i = 0; i < lines.size(); i++) {
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          ret[i] = new Point(maxX - r.width, lastY);
          lastY += r.height;
        }
        break;
      case bottomRight:
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width-xMargin;
        lastY = this.getHeight() - yMargin; // g.getClipBounds().height - yMargin;
        for (int i = lines.size() - 1; i >= 0; i--) {
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          lastY -= r.height;
          ret[i] = new Point(maxX - r.width, lastY);
        }
        break;
      default:
        throw new ENotSupportedException();
    } // switch
    return ret;
  }

}
