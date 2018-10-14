package eng.jAtcSim.BitmapRadar;

import eng.eSystem.events.Event;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.radarBase.ICanvas;
import eng.jAtcSim.radarBase.global.TextBlockLocation;
import eng.jAtcSim.radarBase.global.events.EMouseEventArg;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class BitmapCanvas implements ICanvas<BufferedImage> {
  /**
   * Margin on x axis for text prints.
   */
  private static final int xMargin = 4;
  /**
   * Margin on y axis for text prints.
   */
  private static final int yMargin = -2;
  private final int width;
  private final int height;
  private BufferedImage c;
  private Graphics g;
  private eng.eSystem.events.Event<ICanvas, EMouseEventArg> mouseEvent =
      new eng.eSystem.events.Event<>(this);
  private eng.eSystem.events.EventSimple<ICanvas> paintEvent =
      new eng.eSystem.events.EventSimple<>(this);
  private eng.eSystem.events.Event<ICanvas, Object> keyEvent =
      new eng.eSystem.events.Event<>(this);
  private eng.eSystem.events.EventSimple<ICanvas> resizedEvent =
      new eng.eSystem.events.EventSimple<>(this);

  private final EventSimple<BitmapCanvas> imageDrawn = new EventSimple(this);
  private final EventSimple<BitmapCanvas> imageDrawing = new EventSimple(this);

  public EventSimple<BitmapCanvas> getImageDrawn() {
    return imageDrawn;
  }

  public EventSimple<BitmapCanvas> getImageDrawing() {
    return imageDrawing;
  }

  public BitmapCanvas(int width, int height) {
    this.width = width;
    this.height = height;
    c = null;
    g = null;
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setColor(Coloring.get(color));
    g.drawLine(x1, y1, x2, y2);
  }

  @Override
  public void fillRectangle(int x, int y, int width, int height, eng.jAtcSim.radarBase.global.Color color) {
    g.setColor(Coloring.get(color));
    g.fillRect(x, y, width, height);
  }

  @Override
  public void drawPoint(int x, int y, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setColor(Coloring.get(color));
    int step = width / 2;
    g.fillOval(x - step, y - step, width, width);
  }

  @Override
  public void drawCircleAround(eng.jAtcSim.radarBase.global.Point p, int distanceInPixels, eng.jAtcSim.radarBase.global.Color color, int width) {
    g.setColor(Coloring.get(color));
    int step = distanceInPixels / 2;
    g.drawOval(p.x - step, p.y - step, distanceInPixels, distanceInPixels);
  }

  @Override
  public void drawTriangleAround(eng.jAtcSim.radarBase.global.Point p, int distanceInPixels, eng.jAtcSim.radarBase.global.Color color, int width) {
    eng.jAtcSim.radarBase.global.Point[] pts = new eng.jAtcSim.radarBase.global.Point[3];
    double tStep = distanceInPixels / 3d;
    pts[0] = new eng.jAtcSim.radarBase.global.Point(p.x, p.y - (int) (2 * tStep));

    double xStep = Math.sqrt((2 * tStep) * (2 * tStep) - tStep * tStep);
    pts[1] = new eng.jAtcSim.radarBase.global.Point(p.x - (int) xStep, p.y + (int) tStep);
    pts[2] = new eng.jAtcSim.radarBase.global.Point(p.x + (int) xStep, p.y + (int) tStep);

    drawLine(pts[0], pts[1], color, width);
    drawLine(pts[1], pts[2], color, width);
    drawLine(pts[2], pts[0], color, width);
  }

  @Override
  public void drawCross(eng.jAtcSim.radarBase.global.Point p, eng.jAtcSim.radarBase.global.Color color, int length, int width) {
    int hl = length / 2;

    eng.jAtcSim.radarBase.global.Point topLeft = new eng.jAtcSim.radarBase.global.Point(p.x - hl, p.y - hl);
    eng.jAtcSim.radarBase.global.Point bottomRight = new eng.jAtcSim.radarBase.global.Point(p.x + hl, p.y + hl);
    drawLine(topLeft, bottomRight, color, width);

    eng.jAtcSim.radarBase.global.Point topRight = new eng.jAtcSim.radarBase.global.Point(p.x + hl, p.y - hl);
    eng.jAtcSim.radarBase.global.Point bottomLeft = new eng.jAtcSim.radarBase.global.Point(p.x - hl, p.y + hl);
    drawLine(topRight, bottomLeft, color, width);
  }

  @Override
  public void drawArc(eng.jAtcSim.radarBase.global.Point p, int xRadius, int yRadius, int fromAngle, int toAngle, eng.jAtcSim.radarBase.global.Color color) {
    g.setColor(Coloring.get(color));
    eng.jAtcSim.radarBase.global.Point orig = new eng.jAtcSim.radarBase.global.Point(p.x - xRadius, p.y - yRadius);
    int angleLength = (toAngle < fromAngle) ? (toAngle + 360) : toAngle - fromAngle;
    fromAngle = toEJComponentAngle(fromAngle);
    g.drawArc(orig.x, orig.y, xRadius + xRadius, yRadius + yRadius, fromAngle, -angleLength);
  }

  @Override
  public void drawText(String text, eng.jAtcSim.radarBase.global.Point p, int xShiftInPixels, int yShiftInPixels,
                       eng.jAtcSim.radarBase.global.Font font, eng.jAtcSim.radarBase.global.Color color) {
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

  private static final int ALTITUDE_LINE_SEPARATION_WIDTH = 3;
  @Override
  public void drawAltitudeRangeBoundedAboveAndBelow(eng.jAtcSim.radarBase.global.Point p,
                                                    String minAltitudeLabel, String maxAltitudeLabel,
                                                    int xShiftInPixels, int yShiftInPixels,
                                                    eng.jAtcSim.radarBase.global.Font font, eng.jAtcSim.radarBase.global.Color color) {
    int x = p.x + xShiftInPixels;
    int y = p.y + yShiftInPixels;
    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(color));
    FontMetrics fm = g.getFontMetrics();
    String demoString = minAltitudeLabel == null ? maxAltitudeLabel : minAltitudeLabel;
    Rectangle b = fm.getStringBounds(demoString, g).getBounds();

    int tx;
    int ty;

    if (maxAltitudeLabel != null) {
      tx = x;
      ty = y - b.height - ALTITUDE_LINE_SEPARATION_WIDTH;
      g.drawString(maxAltitudeLabel, tx, ty);

      ty = ty - ALTITUDE_LINE_SEPARATION_WIDTH;
      g.drawLine(tx, ty, tx+b.width, ty);
    }
    if (minAltitudeLabel != null){
      tx = x;
      ty = y + ALTITUDE_LINE_SEPARATION_WIDTH;
      g.drawString(minAltitudeLabel, tx,ty );

      ty = ty + ALTITUDE_LINE_SEPARATION_WIDTH;
      g.drawLine(tx, ty, tx+b.width, ty);
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

    g.setFont(Fonting.get(font));
    g.setColor(Coloring.get(color));

    eng.jAtcSim.radarBase.global.Point[] pts = getPositionsForText(lines, location);
    for (int i = 0; i < lines.size(); i++) {
      g.drawString(lines.get(i), pts[i].x, pts[i].y);
    }
  }

  @Override
  public void clear(eng.jAtcSim.radarBase.global.Color backColor) {
    g.setColor(Coloring.get(backColor));
    g.fillRect(0, 0, this.width, this.height);
  }

  @Override
  public void beforeDraw() {
    this.imageDrawing.raise();
  }

  @Override
  public void afterDraw() {
    this.imageDrawn.raise();
  }

  @Override
  public void invokeRepaint() {
    c = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
    g = c.createGraphics();
    this.paintEvent.raise();
  }

  @Override
  public BufferedImage getGuiControl() {
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
  public eng.jAtcSim.radarBase.global.Size getEstimatedTextSize(eng.jAtcSim.radarBase.global.Font font, int rowsCount, int columnsCount) {
    FontMetrics fm = g.getFontMetrics();

    String str = "0";
    Rectangle2D r = fm.getStringBounds(str, g);

    eng.jAtcSim.radarBase.global.Size ret = new eng.jAtcSim.radarBase.global.Size(
        (int) r.getWidth() * rowsCount,
        (int) r.getHeight() * columnsCount);

    return ret;
  }

  private int toEJComponentAngle(int angle) {
    return 360 - angle + 90;
  }

  private eng.jAtcSim.radarBase.global.Point[] getPositionsForText(List<String> lines, TextBlockLocation location) {

    int lastX;
    int lastY;
    int maxX;
    eng.jAtcSim.radarBase.global.Point[] ret = new eng.jAtcSim.radarBase.global.Point[lines.size()];
    FontMetrics fm = g.getFontMetrics();

    switch (location) {
      case topLeft:
        lastX = xMargin;
        lastY = yMargin + 16;
        for (int i = 0; i < lines.size(); i++) {
          ret[i] = new eng.jAtcSim.radarBase.global.Point(lastX, lastY);
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
          ret[i] = new eng.jAtcSim.radarBase.global.Point(lastX, lastY);
        }
        break;
      case topRight:
        lastY = yMargin + 16;
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width - xMargin;
        for (int i = 0; i < lines.size(); i++) {
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          ret[i] = new eng.jAtcSim.radarBase.global.Point(maxX - r.width, lastY);
          lastY += r.height;
        }
        break;
      case bottomRight:
        maxX = this.getWidth() - xMargin; //g.getClipBounds().width-xMargin;
        lastY = this.getHeight() - yMargin; // g.getClipBounds().height - yMargin;
        for (int i = lines.size() - 1; i >= 0; i--) {
          Rectangle r = fm.getStringBounds(lines.get(i), g).getBounds();
          lastY -= r.height;
          ret[i] = new eng.jAtcSim.radarBase.global.Point(maxX - r.width, lastY);
        }
        break;
      default:
        throw new UnsupportedOperationException();
    } // switch
    return ret;
  }

}
