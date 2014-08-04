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
import jatcsimdraw.shared.es.WithCoordinateEvent;
import jatcsimlib.coordinates.RadarRange;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.world.Area;
import java.awt.event.KeyEvent;

/**
 *
 * @author Marek
 */
public class Radar {

  private final Canvas c;
  private final Visualiser v;
  private final Painter p;
  private final PaintManager m;
  private final EventManager<Radar, EventListener<Radar, WithCoordinateEvent>, WithCoordinateEvent> mouseMoveEM = new EventManager(this);
  private final EventManager<Radar, EventListener<Radar, WithCoordinateEvent>, WithCoordinateEvent> mouseClickEM = new EventManager(this);
  private final EventManager<
      Radar, EventListener<Radar, Object>, Object> paintEM = new EventManager(this);
  private final EventManager<
      Radar, EventListener<Radar, KeyEvent>, KeyEvent> keyPressEM = new EventManager(this);

  public Radar(Canvas canvas, RadarRange radarRange,
      Area area, Settings displaySettings) {
    this.c = canvas;
    this.p = new BasicPainter(c, radarRange.topLeft, radarRange.bottomRight);
    this.v = new BasicVisualiser(p, displaySettings);
    this.m = new PaintManager(area, v);

    this.paintEM.addListener(new EventListener<Radar, Object>() {

      @Override
      public void raise(Radar parent, Object e) {
        m.draw();
      }
    });

    this.c.setFont("Courier CE", 10);
    
    this.c.onMouseEvent().addListener(new EventListener<Canvas, EMouseEvent>() {
      @Override
      public void raise(Canvas parent, EMouseEvent e) {
        Point pt = e.getPoint();
        Coordinate coord = p.toCoordinate(pt);
        switch (e.type) {
          case WheelScroll:
            if (e.wheel > 0) {
              zoomOut();
            } else {
              zoomIn();
            }
            break;
          case Click:
            mouseClickEM.raise(new WithCoordinateEvent(coord));
            break;
          case DoubleClick:
            centerAt (coord);
            break;
          case Move:
            mouseMoveEM.raise(new WithCoordinateEvent(coord));
            break;
          case Drag:
            coord = p.toCoordinateDelta(pt);
            System.out.println("Move by " + coord);
            moveMapBy(coord);
            break;
        }

      }
    });
    this.c.onPaint().addListener(new EventListener<Canvas, Object>() {

      @Override
      public void raise(Canvas parent, Object e) {
        paintEM.raise(null);
      }
    });
    this.c.onKeyPress().addListener(new EventListener<Canvas, KeyEvent>() {

      @Override
      public void raise(Canvas parent, KeyEvent e) {
        processKeyPressAtCanvas(e);
      }
    });
  }

  private void processKeyPressAtCanvas(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_PAGE_DOWN:
        zoomIn();
        break;
      case KeyEvent.VK_PAGE_UP:
        zoomOut();
        break;
      default:
        onKeyPress().raise(e);
    }
  }

  public EventManager<Radar, EventListener<Radar, WithCoordinateEvent>, WithCoordinateEvent> onMouseMove() {
    return mouseMoveEM;
  }

  public EventManager<Radar, EventListener<Radar, WithCoordinateEvent>, WithCoordinateEvent> onMouseClick() {
    return mouseClickEM;
  }

  public EventManager<Radar, EventListener<Radar, KeyEvent>, KeyEvent> onKeyPress() {
    return keyPressEM;
  }

  public EventManager<
      Radar, EventListener<Radar, Object>, Object> onPaint() {
    return paintEM;
  }

  private Point fromAwtPointToMyPoint(java.awt.Point p) {
    return new Point(p.x, p.y);
  }

  public void zoomIn() {
    zoomBy(0.9);
  }

  public void zoomOut() {
    zoomBy(1.1);
  }

  private void zoomBy(double multiplier) {
    double distLat
        = p.getTopLeft().getLatitude().get() - p.getBottomRight().getLatitude().get();
    double distLon
        = p.getTopLeft().getLongitude().get() - p.getBottomRight().getLongitude().get();

    distLat = distLat / 2d;
    distLon = distLon / 2d;

    double distShiftLat = distLat * multiplier - distLat;
    double distShiftLon = distLon * multiplier - distLon;

    p.setCoordinates(
        new Coordinate(
            p.getTopLeft().getLatitude().get() + distShiftLat,
            p.getTopLeft().getLongitude().get() + distShiftLon),
        new Coordinate(
            p.getBottomRight().getLatitude().get() - distShiftLat,
            p.getBottomRight().getLongitude().get() - distShiftLon));
    this.repaint();
  }

  public void centerAt(Coordinate coordinate){
    double distLat
        = p.getTopLeft().getLatitude().get() - p.getBottomRight().getLatitude().get();
    double distLon
        = p.getTopLeft().getLongitude().get() - p.getBottomRight().getLongitude().get();

    distLat = distLat / 2d;
    distLon = distLon / 2d;

    p.setCoordinates(
        new Coordinate(
            coordinate.getLatitude().get() + distLat,
            coordinate.getLongitude().get() + distLon),
        new Coordinate(
            coordinate.getLatitude().get() - distLat,
            coordinate.getLongitude().get() - distLon));
    this.repaint();
  }
  
  public void repaint() {
    c.repaint();
  }

  private void moveMapBy(Coordinate c) {
    p.setCoordinates(
        p.getTopLeft().add(c), 
        p.getBottomRight().add(c));
    repaint();
  }
}
