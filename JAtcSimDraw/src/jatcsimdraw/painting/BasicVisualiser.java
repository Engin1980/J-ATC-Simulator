/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.providers.Coordinates;
import jatcsimlib.types.Coordinate;
import jatcsimlib.world.Border;
import jatcsimlib.world.BorderArcPoint;
import jatcsimlib.world.BorderExactPoint;
import jatcsimlib.world.BorderPoint;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Runway;
import java.awt.Color;

/**
 *
 * @author Marek
 */
public class BasicVisualiser extends Visualiser {

  public BasicVisualiser(Painter p, Settings sett) {
    super(p, sett);
  }

  @Override
  public void drawBorder(Border border) {

    DispSett ds = getDispSettBy(border);

    Coordinate last = null;
    for (int i = 0; i < border.getPoints().size(); i++){
      BorderPoint bp = border.getPoints().get(i);
      if (bp instanceof BorderExactPoint) {
        BorderExactPoint bep = (BorderExactPoint) bp;
        if (last != null) {
          p.drawLine(last, bep.getCoordinate(), ds.getColor(), ds.getWidth());
        }
        last = bep.getCoordinate();
      } else if (bp instanceof BorderArcPoint) {
        BorderExactPoint bPrev = (BorderExactPoint) border.getPoints().get(i-1);
        BorderExactPoint bNext = (BorderExactPoint) border.getPoints().get(i+1);
        drawArc(bPrev, (BorderArcPoint) bp, bNext, ds.getColor());
        last = null;
      } else {
        throw new UnsupportedOperationException();
      }
    }

    if (border.isEnclosed() && !border.getPoints().isEmpty()) {
      BorderExactPoint lastP = (BorderExactPoint) border.getPoints().get(border.getPoints().size() - 1);
      BorderExactPoint firstP = (BorderExactPoint) border.getPoints().get(0);
      p.drawLine(
          lastP.getCoordinate(), firstP.getCoordinate(),
          ds.getColor(), ds.getWidth());
    }
  }

  @Override
  public void drawRunway(Runway runway) {
    DispSett ds = getDispSettBy(runway);

    p.drawLine(
        runway.getThresholdA().getCoordinate(),
        runway.getThresholdB().getCoordinate(),
        ds.getColor(), ds.getWidth());
  }

  @Override
  public void drawNavaid(Navaid navaid) {
    DispSett ds = getDispSettBy(navaid);

    switch (navaid.getType()){
      case VOR:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawCircleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor());
        break;
      case NDB:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawTriangleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor());
        break;
      case Fix:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        //p.drawCircleAround(navaid.getCoordinate(), 9, ds.getColor(), 1);
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, ds.getColor());
        break;
      case FixMinor:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        break;
      case Airport:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor());
        break;
    }
    
  }

  private DispSett getDispSettBy(Border border) {
    switch (border.getType()) {
      case CTR:
        return sett.getDispSett(DispSett.BORDER_CTR);
      case Country:
        return sett.getDispSett(DispSett.BORDER_COUNTRY);
      case TMA:
        return sett.getDispSett(DispSett.BORDER_TMA);
      default:
        throw new ERuntimeException("Border type " + border.getType() + " not implemented.");
    }
  }

  private DispSett getDispSettBy(Runway runway) {
    if (runway.isActive()) {
      return sett.getDispSett(DispSett.RUNWAY_ACTIVE);
    } else {
      return sett.getDispSett(DispSett.RUNWAY_CLOSED);
    }
  }

  private DispSett getDispSettBy(Navaid navaid) {
    switch (navaid.getType()) {
      case Fix:
        return sett.getDispSett(DispSett.NAV_FIX);
      case FixMinor:
        return sett.getDispSett(DispSett.NAV_FIX_MINOR);
      case NDB:
        return sett.getDispSett(DispSett.NAV_NDB);
      case VOR:
        return sett.getDispSett(DispSett.NAV_VOR);
      case Airport:
        return sett.getDispSett(DispSett.NAV_AIRPORT);
      default:
        throw new ERuntimeException("Navaid type " + navaid.getType() + " is not supported.");
    }
  }

  @Override
  public void clear() {
    DispSett ds = sett.getDispSett(DispSett.MAP_BACKCOLOR);
    p.clear(ds.getColor());
  }

  private void drawArc(BorderExactPoint bPrev, BorderArcPoint borderArcPoint, BorderExactPoint bNext, Color color) {
    double startBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
    double endBear = Coordinates.getBearing(borderArcPoint.getCoordinate(), bNext.getCoordinate());
    double distance = Coordinates.getDistanceInNM(borderArcPoint.getCoordinate(), bPrev.getCoordinate());
    if (borderArcPoint.getDirection() == BorderArcPoint.eDirection.counterclockwise){
      double tmp = startBear;
      startBear = endBear;
      endBear = tmp;
    }
    
    p.drawArc(borderArcPoint.getCoordinate(), startBear, endBear, distance, color);
  }

}
