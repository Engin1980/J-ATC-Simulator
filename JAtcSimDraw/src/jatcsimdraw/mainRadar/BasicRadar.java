/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.mainRadar;

import jatcsimdraw.global.Point;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcismdraw.global.radarBase.Canvas;
import jatcismdraw.global.radarBase.Painter;
import jatcismdraw.global.radarBase.Radar;
import jatcismdraw.global.radarBase.Visualiser;
import jatcismdraw.global.radarBase.BasicPainter;
import jatcismdraw.global.radarBase.BasicVisualiser;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimdraw.global.events.EMouseEvent;
import jatcsimdraw.global.events.WithCoordinateEvent;
import jatcsimlib.Simulation;
import jatcsimlib.coordinates.RadarRange;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.world.Area;
import java.awt.event.KeyEvent;

/**
 *
 * @author Marek
 */
public class BasicRadar extends Radar {

  private double heightRange = 1;

  private final Canvas c;
  private final Visualiser v;
  private final Painter p;
  private final PaintManager m;
  private final EventManager<BasicRadar, EventListener<BasicRadar, WithCoordinateEvent>, WithCoordinateEvent> mouseMoveEM = new EventManager(this);
  private final EventManager<BasicRadar, EventListener<BasicRadar, WithCoordinateEvent>, WithCoordinateEvent> mouseClickEM = new EventManager(this);
  private final EventManager<
      BasicRadar, EventListener<BasicRadar, Object>, Object> paintEM = new EventManager(this);
  private final EventManager<
      BasicRadar, EventListener<BasicRadar, KeyEvent>, KeyEvent> keyPressEM = new EventManager(this);

  public BasicRadar(Canvas canvas, RadarRange radarRange,
    Simulation sim, Area area, Settings displaySettings) {
    this.c = canvas;
    this.p = new BasicPainter(c, radarRange.topLeft, radarRange.bottomRight);
    this.v = new BasicVisualiser(p, displaySettings);
    this.m = new PaintManager(sim, area, v,
        displaySettings.getMessageVisibleDelayInRadarCycles(),
        displaySettings.getFormatter());

    this.paintEM.addListener(new EventListener<BasicRadar, Object>() {

      @Override
      public void raise(BasicRadar parent, Object e) {
        m.draw();
      }
    });

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
            centerAt(coord);
            break;
          case Move:
            mouseMoveEM.raise(new WithCoordinateEvent(coord));
            break;
          case Drag:
            // drag bez priznaku je posun mapy, jinak je to posun letadla
            if (e.modifiers.is(false, false, false) && e.button == EMouseEvent.eButton.right) {
              coord = p.toCoordinateDelta(e.getDropRangePoint());
              moveMapBy(coord);
            }
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

  public EventManager<BasicRadar, EventListener<BasicRadar, WithCoordinateEvent>, WithCoordinateEvent> onMouseMove() {
    return mouseMoveEM;
  }

  public EventManager<BasicRadar, EventListener<BasicRadar, WithCoordinateEvent>, WithCoordinateEvent> onMouseClick() {
    return mouseClickEM;
  }

  public EventManager<BasicRadar, EventListener<BasicRadar, KeyEvent>, KeyEvent> onKeyPress() {
    return keyPressEM;
  }

  public EventManager<
      BasicRadar, EventListener<BasicRadar, Object>, Object> onPaint() {
    return paintEM;
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

  public void centerAt(Coordinate coordinate) {
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

    Coordinate topLeft = p.getTopLeft();

    //TODO tohle musim vyresit, protoze s timhle nefunguje zoomovani a bez tohodle zase zmena velikosti okna
//    double widthRange = heightRange * (c.getWidth() / (double) c.getHeight());
//    
//    Coordinate bottomRight = new Coordinate(
//      topLeft.getLatitude().add(-heightRange),
//      topLeft.getLongitude().add(widthRange));
//    p.setCoordinates(topLeft, bottomRight);
    c.repaint();
  }

  private void moveMapBy(Coordinate c) {
    p.setCoordinates(
      p.getTopLeft().add(c),
      p.getBottomRight().add(c));
    repaint();
  }
}
