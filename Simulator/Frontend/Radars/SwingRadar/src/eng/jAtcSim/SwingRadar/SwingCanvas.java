package eng.jAtcSim.SwingRadar;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.events.Event;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.abstractRadar.ICanvas;
import eng.jAtcSim.abstractRadar.global.Color;
import eng.jAtcSim.abstractRadar.global.Font;
import eng.jAtcSim.abstractRadar.global.Point;
import eng.jAtcSim.abstractRadar.global.TextBlockLocation;
import eng.jAtcSim.abstractRadar.global.events.EKeyboardModifier;
import eng.jAtcSim.abstractRadar.global.events.EMouseEventArg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

public class SwingCanvas implements ICanvas<JComponent> {

  static class EditablePoint {
    public int x;
    public int y;

    public EditablePoint(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return "EditablePoint{" +
          "x=" + x +
          ", y=" + y +
          '}';
    }
  }

  class MouseProcessor {
    private java.awt.Point dragStartPoint = null;
    private int dragStartModifiers = 0;
    private int dragStartButton = 0;
    private int MINIMUM_DRAG_SHIFT = 3;

    void clicked(MouseEvent e) {
      EMouseEventArg eme;
      EMouseEventArg.eType type;
      if (e.getClickCount() == 2) {
        type = EMouseEventArg.eType.doubleClick;
      } else {
        type = EMouseEventArg.eType.click;

      }
      EMouseEventArg.eButton btn = convertFromSwingButton(e.getButton());
      eme = EMouseEventArg.createClick(
          e.getPoint().x, e.getPoint().y, type,
          btn, new EKeyboardModifier(dragStartModifiers));
      raiseEvent(eme);
    }

    private EMouseEventArg.eButton convertFromSwingButton(int button) {
      EMouseEventArg.eButton ret;
      switch (button) {
        case 0:
          ret = EMouseEventArg.eButton.none;
          break;
        case java.awt.event.MouseEvent.BUTTON1:
          ret = EMouseEventArg.eButton.left;
          break;
        case java.awt.event.MouseEvent.BUTTON2:
          ret = EMouseEventArg.eButton.middle;
          break;
        case java.awt.event.MouseEvent.BUTTON3:
          ret = EMouseEventArg.eButton.right;
          break;
        default:
          ret = EMouseEventArg.eButton.other;
          break;
      }
      return ret;
    }

    void pressed(MouseEvent e) {
      dragStartPoint = e.getPoint();
      dragStartModifiers = e.getModifiers();
      dragStartButton = e.getButton();
    }

    void released(MouseEvent e) {
      if (dragStartPoint == null) {
        return;
      }
      java.awt.Point dragEndPoint = e.getPoint();
      Point diffPoint = new Point(
          Math.abs(dragEndPoint.x - dragStartPoint.x),
          Math.abs(dragEndPoint.y - dragStartPoint.y));
      if (diffPoint.x < MINIMUM_DRAG_SHIFT && diffPoint.y < MINIMUM_DRAG_SHIFT) {
        // intentionally blank
      } else {
        EMouseEventArg.eButton btn = convertFromSwingButton(e.getButton());
        EMouseEventArg eme = EMouseEventArg.createDragged(
            dragStartPoint.x, dragStartPoint.y, dragEndPoint.x, dragEndPoint.y,
            btn, new EKeyboardModifier(dragStartModifiers));
        dragStartModifiers = 0;
        dragStartPoint = null;
        raiseEvent(eme);
      }
    }

    void dragged(MouseEvent e) {
      java.awt.Point dragEndPoint = e.getPoint();
      Point diffPoint = new Point(
          Math.abs(dragEndPoint.x - dragStartPoint.x),
          Math.abs(dragEndPoint.y - dragStartPoint.y));
      if (diffPoint.x < MINIMUM_DRAG_SHIFT && diffPoint.y < MINIMUM_DRAG_SHIFT) {
        return;
      }
      EMouseEventArg.eButton btn = convertFromSwingButton(dragStartButton);
      EMouseEventArg eme = EMouseEventArg.createDragging(
          dragStartPoint.x, dragStartPoint.y, dragEndPoint.x, dragEndPoint.y,
          btn, new EKeyboardModifier(dragStartModifiers));

      raiseEvent(eme);
    }

    void moved(MouseEvent e) {
      EMouseEventArg eme = EMouseEventArg.createMove(e.getX(), e.getY());
      raiseEvent(eme);
    }

    void scrolled(MouseWheelEvent e) {
      if (e.getWheelRotation() == 0) return;
      EMouseEventArg eme = EMouseEventArg.createScroll(
          e.getPoint().x, e.getPoint().y, e.getWheelRotation());
      SwingCanvas.this.mouseEvent.raise(eme);
    }

    private void raiseEvent(EMouseEventArg eme) {
      SwingCanvas.this.mouseEvent.raise(eme);
    }
  }

  private static final double MAX_TEXT_WIDTH_RATIO = .45;
  private static final double MAX_TEXT_TOP_RIGHT_WIDTH_RATIO = .80;
  private static final int TEXT_WIDTH_MARGIN = 16;
  private static final int TEXT_HEIGHT_MARGIN = 0;
  private final JComponent c;
  private Graphics g;
  private final eng.eSystem.events.Event<ICanvas<?>, EMouseEventArg> mouseEvent =
      new eng.eSystem.events.Event<>(this);
  private final eng.eSystem.events.EventSimple<ICanvas<?>> paintEvent =
      new eng.eSystem.events.EventSimple<>(this);
  private final Event<ICanvas<?>, Object> keyEvent =
      new Event<>(this);
  private final eng.eSystem.events.EventSimple<ICanvas<?>> resizedEvent =
      new eng.eSystem.events.EventSimple<>(this);
  private final MouseProcessor mp = this.new MouseProcessor();

  private static String getTheFirstLine(String line) {
    int lineBreakIndex = line.indexOf('\n');
    if (lineBreakIndex > 0)
      line = line.substring(0, lineBreakIndex);
    return line;
  }

  public SwingCanvas() {
    this.c = new JPanel() {
      @Override
      public void paintComponent(Graphics g) {
        SwingCanvas.this.g = g;
        SwingCanvas.this.paintEvent.raise();
      }
    };
    this.c.setFocusable(true);

    MouseAdapter ma;
    ma = new MouseAdapter() {


      @Override
      public void mouseClicked(MouseEvent e) {
        SwingCanvas.this.mp.clicked(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        mp.pressed(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        mp.released(e);
      }
    };
    c.addMouseListener(ma);

    ma = new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        mp.dragged(e);
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        mp.moved(e);
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

    c.addMouseWheelListener(e -> mp.scrolled(e));

    c.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        resizedEvent.raise();
      }
    });
  }

  @Override
  public int getWidth() {
    if (g != null)
      return g.getClipBounds().width;
    else
      return 1;
  }

  @Override
  public int getHeight() {
    if (g != null)
      return g.getClipBounds().height;
    else
      return 1;
  }

  @Override
  public boolean isReady() {
    return g != null && g.getClipBounds().width == c.getWidth();
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
  public void drawText(String text, Point p, int xShiftInPixels, int yShiftInPixels, Font font, Color color) {
    String[] lines = text.split(System.getProperty("line.separator"));

    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;

    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(color));

    for (String line : lines) {
      FontMetrics fm = g.getFontMetrics();
      Rectangle b = fm.getStringBounds(line, g).getBounds();

      y = y + b.height - 5;

      g.drawString(line, x, y);
    }
  }

  @Override
  public void drawTextBlock(List<String> lines, TextBlockLocation location, Font font, Color color) {
    if (lines.isEmpty()) {
      return;
    } else if (location.isBottom())
      Collections.reverse(lines);

    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(color));

    int width = (int) g.getClipBounds().getWidth() - TEXT_WIDTH_MARGIN;
    double maxLength = location == TextBlockLocation.topRight ?
        width * MAX_TEXT_TOP_RIGHT_WIDTH_RATIO : width * MAX_TEXT_WIDTH_RATIO;
    int height = (int) g.getClipBounds().getHeight() - TEXT_HEIGHT_MARGIN;

    IList<Tuple<EditablePoint, String>> positionedLines = getPositionedLines(lines, location, width, height, maxLength);
    for (Tuple<EditablePoint, String> line : positionedLines) {
      g.drawString(line.getB(), line.getA().x, line.getA().y);
    }
  }

  @Override
  public void clear(Color backColor) {
    int h = getHeight();
    int w = getWidth();
    g.setColor(Coloring.get(backColor));
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
    this.c.repaint();
  }

  @Override
  public JComponent getGuiControl() {
    return this.c;
  }

  @Override
  public eng.eSystem.events.Event<ICanvas<?>, EMouseEventArg> getMouseEvent() {
    return mouseEvent;
  }

  @Override
  public eng.eSystem.events.EventSimple<ICanvas<?>> getPaintEvent() {
    return paintEvent;
  }

  @Override
  public Event<ICanvas<?>, Object> getKeyEvent() {
    return keyEvent;
  }

  @Override
  public EventSimple<ICanvas<?>> getResizedEvent() {
    return resizedEvent;
  }

  @Override
  public eng.jAtcSim.abstractRadar.global.Size getEstimatedTextSize(Font font, int rowsCount, int columnsCount) {
    FontMetrics fm = g.getFontMetrics();

    String str = "0";
    Rectangle2D r = fm.getStringBounds(str, g);


    eng.jAtcSim.abstractRadar.global.Size ret = new eng.jAtcSim.abstractRadar.global.Size(
        (int) r.getWidth() * rowsCount,
        (int) r.getHeight() * columnsCount);

    return ret;
  }

  @Override
  public Rectangle getStringBounds(String text, Font font) {
    java.awt.Font awtFont = Fonting.get(font);
    FontMetrics fm = g.getFontMetrics(awtFont);
    Rectangle ret = fm.getStringBounds(text, g).getBounds();
    return ret;
  }

  private IList<Tuple<EditablePoint, String>> getPositionedLines(List<String> lines, TextBlockLocation location,
                                                                 int width, int height, double maxLength) {
    IList<Tuple<EditablePoint, String>> ret = new EList<>();

    FontMetrics fm = g.getFontMetrics();
    int lineStep = fm.getAscent(); // see https://docs.oracle.com/javase/tutorial/2d/text/measuringtext.html
    if (location.isBottom()) lineStep = -lineStep;
    int lineMid = lineStep / 2;
    int globalY = getGlobalY(location, lineStep, height);
    for (String line : lines) {
      IList<Tuple<EditablePoint, String>> tmp = convertToSublines(line, fm, maxLength);
      if (location.isBottom()) tmp.reverse(); // to correct printing order
      adjustLineRelativePosition(tmp, location, width);
      globalY = adjustLineAbsolutePosition(tmp, globalY, lineStep);
      ret.add(tmp);
      globalY += lineMid;
    }

    return ret;
  }

  private int adjustLineAbsolutePosition(IList<Tuple<EditablePoint, String>> tmp, int globalY, int lineStep) {
    for (Tuple<EditablePoint, String> tmpItem : tmp) {
      tmpItem.getA().y = globalY;
      globalY += lineStep;
    }
    return globalY;
  }

  private int getGlobalY(TextBlockLocation location, int lineStep, int maxHeight) {
    if (location.isBottom())
      return maxHeight + lineStep;
    else
      return lineStep;
  }

  private void adjustLineRelativePosition(IList<Tuple<EditablePoint, String>> tmp, TextBlockLocation location, int maxWidth) {
//    int curY = 8 + lineStep;
//    for (Tuple<EditablePoint, String> tmpItem : tmp) {
//      tmpItem.getA().y = curY;
//      curY += lineStep;
//    }
    if (location.isRight() == false)
      for (Tuple<EditablePoint, String> tmpItem : tmp) {
        tmpItem.getA().x = 8;
      }
    else
      for (Tuple<EditablePoint, String> tmpItem : tmp) {
        tmpItem.getA().x = maxWidth - tmpItem.getA().x;
      }
  }

  private IList<Tuple<EditablePoint, String>> convertToSublines(String line, FontMetrics fm, double maxWidth) {
    IList<Tuple<EditablePoint, String>> ret = new EList<>();
    Rectangle r = fm.getStringBounds(line, g).getBounds();
    if (r.width < maxWidth)
      ret.add(new Tuple<>(new EditablePoint(r.width, 0), line));
    else {
      while (line.isEmpty() == false) {
        Tuple<String, Integer> subLineInfo = getSubLineInWidth(line, fm, maxWidth);
        ret.add(new Tuple<>(new EditablePoint(subLineInfo.getB(), 0), subLineInfo.getA()));
        line = line.substring(subLineInfo.getA().length()).trim();
      }
    }
    return ret;
  }

  private Tuple<String, Integer> getSubLineInWidth(String line, FontMetrics fm, double maxWidth) {
    Tuple<String, Integer> ret = new Tuple<>("", 0);
    String tmpLine = getTheFirstLine(line);
    int currentWidth = fm.getStringBounds(tmpLine, g).getBounds().width;
    while (true) {
      if (currentWidth < maxWidth) {
        // optimal, fits into the width
        ret.setA(tmpLine);
        ret.setB(currentWidth);
        break;
      } else if (tmpLine.isEmpty()) {
        // empty, nothing remains
        ret.setA(line);
        ret.setB(fm.getStringBounds(line, g).getBounds().width);
        break;
      } else {
        int spaceIndex = tmpLine.lastIndexOf(' ');
        if (spaceIndex < 0)
          tmpLine = "";
        else
          tmpLine = tmpLine.substring(0, spaceIndex);
      }
      currentWidth = fm.getStringBounds(tmpLine, g).getBounds().width;
    }
    return ret;
  }

  private int toEJComponentAngle(int angle) {
    return 360 - angle + 90;
  }

}
