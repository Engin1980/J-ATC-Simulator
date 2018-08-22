package eng.jAtcSim.javaFXRadar;

import eng.eSystem.events.Event;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.radarBase.ICanvas;
import eng.jAtcSim.radarBase.global.Point;
import eng.jAtcSim.radarBase.global.Size;
import eng.jAtcSim.radarBase.global.TextBlockLocation;
import eng.jAtcSim.radarBase.global.events.EKeyboardModifier;
import eng.jAtcSim.radarBase.global.events.EMouseEventArg;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.awt.event.MouseWheelEvent;
import java.util.List;

public class JavaFXCanvas implements ICanvas<Canvas> {

  class MouseProcessor {
    private Point dragStartPoint = null;
    private int dragStartModifiers = 0;
    private MouseButton dragStartButton = MouseButton.NONE;
    private int MINIMUM_DRAG_SHIFT = 3;

    public MouseProcessor() {
      JavaFXCanvas.this.c.addEventHandler(MouseEvent.MOUSE_CLICKED, q -> this.clicked(q));
      JavaFXCanvas.this.c.addEventHandler(MouseEvent.MOUSE_PRESSED, q -> this.pressed(q));
      JavaFXCanvas.this.c.addEventHandler(MouseEvent.MOUSE_RELEASED, q -> this.released(q));
      JavaFXCanvas.this.c.addEventHandler(MouseEvent.MOUSE_DRAGGED, q -> this.dragged(q));
      JavaFXCanvas.this.c.addEventHandler(MouseEvent.MOUSE_MOVED, q -> this.moved(q));
      //JavaFXCanvas.this.c.addEventHandler(MouseEvent., q->this.scrolled(q) );
    }

    void clicked(MouseEvent e) {
      EMouseEventArg eme;
      EMouseEventArg.eType type;
      if (e.getClickCount() == 2) {
        type = EMouseEventArg.eType.doubleClick;
      } else {
        type = EMouseEventArg.eType.click;

      }
      EMouseEventArg.eButton btn = convertToMouseButton(e.getButton());
      eme = EMouseEventArg.createClick(
          (int) e.getX(), (int) e.getY(), type, btn,
          new EKeyboardModifier(dragStartModifiers));
      raiseEvent(eme);
    }

    private EMouseEventArg.eButton convertToMouseButton(MouseButton button) {
      EMouseEventArg.eButton ret;
      switch (button) {
        case NONE:
          ret = EMouseEventArg.eButton.none;
          break;
        case PRIMARY:
          ret = EMouseEventArg.eButton.left;
          break;
        case SECONDARY:
          ret = EMouseEventArg.eButton.right;
          break;
        case MIDDLE:
          ret = EMouseEventArg.eButton.middle;
          break;
        default:
          ret = EMouseEventArg.eButton.other;
          break;
      }
      return ret;
    }

    void pressed(MouseEvent e) {
      dragStartPoint = new Point((int) e.getX(), (int) e.getY());
      dragStartModifiers = (e.isAltDown() ? 2 : 0) + (e.isControlDown() ? 4 : 0) + (e.isShiftDown() ? 8 : 0);
      dragStartButton = e.getButton();
    }

    void released(MouseEvent e) {
      if (dragStartPoint == null) {
        return;
      }
      Point dragEndPoint = new Point((int) e.getX(), (int) e.getY());
      Point diffPoint = new Point(
          Math.abs(dragEndPoint.x - dragStartPoint.x),
          Math.abs(dragEndPoint.y - dragStartPoint.y));
      if (diffPoint.x < MINIMUM_DRAG_SHIFT && diffPoint.y < MINIMUM_DRAG_SHIFT) {
      } else {
        EMouseEventArg.eButton btn = convertToMouseButton(e.getButton());
        EMouseEventArg eme = EMouseEventArg.createDragged(
            dragStartPoint.x, dragStartPoint.y, dragEndPoint.x, dragEndPoint.y, btn,
            new EKeyboardModifier(dragStartModifiers));
        dragStartModifiers = 0;
        dragStartPoint = null;
        raiseEvent(eme);
      }
    }

    void dragged(MouseEvent e) {
      Point dragEndPoint = new Point((int) e.getX(), (int) e.getY());
      Point diffPoint = new Point(
          Math.abs(dragEndPoint.x - dragStartPoint.x),
          Math.abs(dragEndPoint.y - dragStartPoint.y));
      if (diffPoint.x < MINIMUM_DRAG_SHIFT && diffPoint.y < MINIMUM_DRAG_SHIFT) {
        return;
      }
      EMouseEventArg.eButton btn = convertToMouseButton(e.getButton());
      EMouseEventArg eme = EMouseEventArg.createDragging(
          dragStartPoint.x, dragStartPoint.y, dragEndPoint.x, dragEndPoint.y,
          btn, new EKeyboardModifier(dragStartModifiers));

      raiseEvent(eme);
    }

    void moved(MouseEvent e) {
      EMouseEventArg eme = EMouseEventArg.createMove((int) e.getX(), (int) e.getY());
      raiseEvent(eme);
    }

    void scrolled(MouseWheelEvent e) {
      EMouseEventArg eme = EMouseEventArg.createScroll(
          e.getPoint().x, e.getPoint().y, e.getWheelRotation());
      JavaFXCanvas.this.mouseEvent.raise(eme);
    }

    private void raiseEvent(EMouseEventArg eme) {
      JavaFXCanvas.this.mouseEvent.raise(eme);
    }
  }

  /**
   * Margin on x axis for text prints.
   */
  private static final int xMargin = 4;
  /**
   * Margin on y axis for text prints.
   */
  private static final int yMargin = -2;
  private final Canvas c;
  private GraphicsContext g;
  private eng.eSystem.events.Event<ICanvas, EMouseEventArg> mouseEvent =
      new eng.eSystem.events.Event<>(this);
  private eng.eSystem.events.EventSimple<ICanvas> paintEvent =
      new eng.eSystem.events.EventSimple<>(this);
  private eng.eSystem.events.Event<ICanvas, Object> keyEvent =
      new eng.eSystem.events.Event<>(this);
  private eng.eSystem.events.EventSimple<ICanvas> resizedEvent =
      new eng.eSystem.events.EventSimple<>(this);
  private MouseProcessor mp = this.new MouseProcessor();

  public JavaFXCanvas() {

    this.c = new Canvas();
    this.g = this.c.getGraphicsContext2D();

    //TODO
    //this.c.setFocusable(true);

//    MouseAdapter ma;
//    ma = new MouseAdapter() {
//
//
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        SwingCanvas.this.mp.clicked(e);
//      }
//
//      @Override
//      public void mousePressed(MouseEvent e) {
//        mp.pressed(e);
//      }
//
//      @Override
//      public void mouseReleased(MouseEvent e) {
//        mp.released(e);
//      }
//    };
//    c.addMouseListener(ma);
//
//    ma = new MouseAdapter() {
//      @Override
//      public void mouseMoved(MouseEvent e) {
//        mp.moved(e);
//      }
//
//      @Override
//      public void mouseDragged(MouseEvent e) {
//        mp.dragged(e);
//      }
//    };
//    c.addMouseMotionListener(ma);
//
//    KeyAdapter ka;
//    ka = new KeyAdapter() {
//
//      @Override
//      public void keyPressed(KeyEvent e) {
//        SwingCanvas.this.keyEvent.raise(e);
//      }
//    };
//    c.addKeyListener(ka);
//
//    c.addMouseWheelListener(e -> mp.scrolled(e));
//
//    c.addComponentListener(new ComponentAdapter() {
//      @Override
//      public void componentResized(ComponentEvent e) {
//        resizedEvent.raise();
//      }
//    });
  }

  @Override
  public int getWidth() {
    int ret = (int) c.getWidth();
    return ret;
//    if (g != null)
//      return g.getClipBounds().width;
//    else
//      return 1;
  }

  @Override
  public int getHeight() {
    int ret = (int) c.getHeight();
    return ret;
//    if (g != null)
//      return g.getClipBounds().height;
//    else
//      return 1;
  }

  @Override
  public boolean isReady() {
    return g != null;
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setStroke(Coloring.get(color));
    g.strokeLine(x1, y1, x2, y2);
  }

  @Override
  public void fillRectangle(int x, int y, int width, int height, eng.jAtcSim.radarBase.global.Color color) {
    g.setFill(Coloring.get(color));
    g.fillRect(x, y, width, height);
  }

  @Override
  public void drawPoint(int x, int y, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setFill(Coloring.get(color));
    int step = width / 2;
    g.fillOval(x - step, y - step, width, width);
  }

  @Override
  public void drawCircleAround(Point p, int distanceInPixels, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setStroke(Coloring.get(color));
    int step = distanceInPixels / 2;
    g.strokeOval(p.x - step, p.y - step, distanceInPixels, distanceInPixels);
  }

  @Override
  public void drawTriangleAround(Point p, int distanceInPixels, eng.jAtcSim.radarBase.global.Color color, int width) {
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
  public void drawCross(Point p, eng.jAtcSim.radarBase.global.Color color, int length, int width) {
    int hl = length / 2;

    Point topLeft = new Point(p.x - hl, p.y - hl);
    Point bottomRight = new Point(p.x + hl, p.y + hl);
    drawLine(topLeft, bottomRight, color, width);

    Point topRight = new Point(p.x + hl, p.y - hl);
    Point bottomLeft = new Point(p.x - hl, p.y + hl);
    drawLine(topRight, bottomLeft, color, width);
  }

  @Override
  public void drawArc(Point p, int xRadius, int yRadius, int fromAngle, int toAngle, eng.jAtcSim.radarBase.global.Color color) {
    g.setStroke(Coloring.get(color));
    Point orig = new Point(p.x - xRadius, p.y - yRadius);
    int angleLength = (toAngle < fromAngle) ? (toAngle + 360) : toAngle - fromAngle;
    fromAngle = toEJComponentAngle(fromAngle);
    g.strokeArc(orig.x, orig.y, xRadius + xRadius, yRadius + yRadius, fromAngle, -angleLength, ArcType.OPEN);
  }

  @Override
  public void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, eng.jAtcSim.radarBase.global.Font font, eng.jAtcSim.radarBase.global.Color c) {
    String[] lines = text.split(System.getProperty("line.separator"));

    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;

    Font fxFont = Fonting.get(font);
    g.setFont(fxFont);
    g.setFill(Coloring.get(c));

    for (String line : lines) {

      Bounds b = getTextBounds(line, fxFont);
      y = y + (int) b.getHeight() - 5;

      g.fillText(line, x, y);
    }
  }

  @Override
  public void drawTextBlock(java.util.List<String> lines, TextBlockLocation location, eng.jAtcSim.radarBase.global.Font font, eng.jAtcSim.radarBase.global.Color color) {
    if (location == TextBlockLocation.bottomMiddle
        || location == TextBlockLocation.middleLeft
        || location == TextBlockLocation.middleRight
        || location == TextBlockLocation.topMiddle) {
      //TODO remove this, move this exception to somewhere shared and remove
      // dependency on JAtcSimLib
      throw new UnsupportedOperationException();
    }
    if (lines.isEmpty()) {
      return;
    }

    Font fxFont = Fonting.get(font);
    g.setFont(fxFont);
    g.setFill(Coloring.get(color));

    Point[] pts = getPositionsForText(lines, location, fxFont);
    for (int i = 0; i < lines.size(); i++) {
      g.fillText(lines.get(i), pts[i].x, pts[i].y);
    }
  }

  @Override
  public void clear(eng.jAtcSim.radarBase.global.Color backColor) {
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
  public void invokeRepaint() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Canvas getGuiControl() {
    return this.c;
  }

  @Override
  public eng.eSystem.events.Event<ICanvas, EMouseEventArg> getMouseEvent() {
    return mouseEvent;
  }

  @Override
  public eng.eSystem.events.EventSimple<ICanvas> getPaintEvent() {
    return paintEvent;
  }

  @Override
  public Event<ICanvas, Object> getKeyEvent() {
    return keyEvent;
  }

  @Override
  public EventSimple<ICanvas> getResizedEvent() {
    return resizedEvent;
  }

  @Override
  public Size getEstimatedTextSize(eng.jAtcSim.radarBase.global.Font font, int rowsCount, int columnsCount) {
    throw new UnsupportedOperationException("This method is not implemented, but should be.");
  }

  public Bounds getTextBounds(String s, Font font) {
    Text text = new Text(s);
    text.setFont(font);
    Bounds tb = text.getBoundsInLocal();
    Rectangle stencil = new Rectangle(
        tb.getMinX(), tb.getMinY(), tb.getWidth(), tb.getHeight()
    );

    Shape intersection = Shape.intersect(text, stencil);

    Bounds ret = intersection.getBoundsInLocal();
    return ret;
  }

  private int toEJComponentAngle(int angle) {
    return 360 - angle + 90;
  }

  private Point[] getPositionsForText(List<String> lines, TextBlockLocation location, Font font) {

    int lastX;
    int lastY;
    int maxX;
    Point[] ret = new Point[lines.size()];

    switch (location) {
      case topLeft:
        lastX = xMargin;
        lastY = yMargin + 16;
        for (int i = 0; i < lines.size(); i++) {
          ret[i] = new Point(lastX, lastY);
          Bounds b = getTextBounds(lines.get(i), font);
          lastY += b.getHeight();
        }
        break;
      case bottomLeft:
        lastX = xMargin;
        lastY = this.getHeight() - yMargin; // g.getClipBounds().height - yMargin;
        for (int i = lines.size() - 1; i >= 0; i--) {
          Bounds b = getTextBounds(lines.get(i), font);
          lastY -= b.getHeight();
          ret[i] = new Point(lastX, lastY);
        }
        break;
      case topRight:
        lastY = yMargin + 16;
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width - xMargin;
        for (int i = 0; i < lines.size(); i++) {
          Bounds b = getTextBounds(lines.get(i), font);
          ret[i] = new Point(maxX - (int) b.getWidth(), lastY);
          lastY += b.getHeight();
        }
        break;
      case bottomRight:
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width-xMargin;
        lastY = this.getHeight() - yMargin; // g.getClipBounds().height - yMargin;
        for (int i = lines.size() - 1; i >= 0; i--) {
          Bounds b = getTextBounds(lines.get(i), font);
          lastY -= b.getHeight();
          ret[i] = new Point(maxX - (int) b.getWidth(), lastY);
        }
        break;
      default:
        throw new UnsupportedOperationException();
    } // switch
    return ret;
  }

}
