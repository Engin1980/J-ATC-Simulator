/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcismdraw.radarBase;

import jatcsimdraw.settings.DispItem;
import jatcsimdraw.settings.DispPlane;
import jatcsimdraw.settings.DispText;
import jatcsimdraw.settings.Settings;
import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.Headings;
import jatcsimlib.messaging.Message;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Border;
import jatcsimlib.world.BorderArcPoint;
import jatcsimlib.world.BorderExactPoint;
import jatcsimlib.world.BorderPoint;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Runway;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class BasicVisualiser extends Visualiser {

  public BasicVisualiser(Painter p, Settings sett) {
    super(p, sett);
  }

  @Override
  public void drawMessages(List<Message> msgs) {
    MessageSet ms = createMessageSet(msgs);
    
    DispText dt;
    
    dt = sett.getDispText(DispText.eType.atc);
    p.drawTextBlock (ms.atc, Painter.eTextBlockLocation.bottomRight, dt.getColor(), Painter.eTextType.message);
    
    dt = sett.getDispText(DispText.eType.plane);
    p.drawTextBlock(ms.plane, Painter.eTextBlockLocation.bottomLeft, dt.getColor(), Painter.eTextType.message );
    
    dt = sett.getDispText(DispText.eType.system);
    p.drawTextBlock(ms.system, Painter.eTextBlockLocation.topRight, dt.getColor(), Painter.eTextType.message);    
  }
  
  @Override
  public void drawBorder(Border border) {

    DispItem ds = getDispSettBy(border);

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
    DispItem ds = getDispSettBy(runway);

    p.drawLine(
        runway.getThresholdA().getCoordinate(),
        runway.getThresholdB().getCoordinate(),
        ds.getColor(), ds.getWidth());
  }

  @Override
  public void drawNavaid(Navaid navaid) {
    DispItem ds = getDispSettBy(navaid);

    switch (navaid.getType()){
      case VOR:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawCircleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor(), Painter.eTextType.navaid);
        break;
      case NDB:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawTriangleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor(), Painter.eTextType.navaid);
        break;
      case Fix:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        //p.drawCircleAround(navaid.getCoordinate(), 9, ds.getColor(), 1);
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, ds.getColor(), Painter.eTextType.navaid);
        break;
      case FixMinor:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        break;
      case Airport:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, ds.getColor(), Painter.eTextType.navaid);
        break;
    }
    
  }

  private DispItem getDispSettBy(Border border) {
    switch (border.getType()) {
      case CTR:
        return sett.getDispItem(DispItem.BORDER_CTR);
      case Country:
        return sett.getDispItem(DispItem.BORDER_COUNTRY);
      case TMA:
        return sett.getDispItem(DispItem.BORDER_TMA);
      default:
        throw new ERuntimeException("Border type " + border.getType() + " not implemented.");
    }
  }

  private DispItem getDispSettBy(Runway runway) {
    if (runway.isActive()) {
      return sett.getDispItem(DispItem.RUNWAY_ACTIVE);
    } else {
      return sett.getDispItem(DispItem.RUNWAY_CLOSED);
    }
  }

  private DispItem getDispSettBy(Navaid navaid) {
    switch (navaid.getType()) {
      case Fix:
        return sett.getDispItem(DispItem.NAV_FIX);
      case FixMinor:
        return sett.getDispItem(DispItem.NAV_FIX_MINOR);
      case NDB:
        return sett.getDispItem(DispItem.NAV_NDB);
      case VOR:
        return sett.getDispItem(DispItem.NAV_VOR);
      case Airport:
        return sett.getDispItem(DispItem.NAV_AIRPORT);
      default:
        throw new ERuntimeException("Navaid type " + navaid.getType() + " is not supported.");
    }
  }

  @Override
  public void clear() {
    DispItem ds = sett.getDispItem(DispItem.MAP_BACKCOLOR);
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

  @Override
  public void drawPlane(Airplane plane, Atc.eType responsibleAtcType) {
    
    DispPlane dp = sett.getDispPlane(responsibleAtcType);
    
    p.drawPoint(plane.getCoordinate(), dp.getColor(), dp.getPointWidth());
    p.drawLine(plane.getCoordinate(), dp.getHeadingLineLength(), plane.getHeading(), 
        dp.getColor(), 1); // 1 = width
    
    StringBuilder sb = new StringBuilder();
    sb.append(
      buildPlaneString(dp.getFirstLineFormat(), plane));
    sb.append("\r\n");
    sb.append(
      buildPlaneString(dp.getSecondLineFormat(), plane));
    sb.append("\r\n");
    sb.append(
      buildPlaneString(dp.getThirdLineFormat(), plane));
    
    p.drawText(sb.toString(), plane.getCoordinate(), 3, 3, dp.getColor(), Painter.eTextType.plane);
        
  }

  private String buildPlaneString(String lineFormat, Airplane plane) {
    String ret = plane.getInfo().format(lineFormat);
    return ret;
  }

  private MessageSet createMessageSet(List<Message> msgs) {
    MessageSet ret = new MessageSet();
    
    for (Message m : msgs){
      if (m.isSystemMessage())
        ret.system.add(">> " + m.getAsString().text);
      else if (m.isAtcMessage()){
        Atc atc = (Atc) m.source;
        if (atc.getType() == Atc.eType.twr)
          ret.atc.add("TWR: " + m.getAsPlaneSwitchMessage().getAsString());
        else if (atc.getType() == Atc.eType.ctr)
          ret.atc.add("CTR : " + m.getAsPlaneSwitchMessage().getAsString());
        else
          throw new ENotSupportedException();
      }
      else if (m.isPlaneMessage()){
        Airplane p = (Airplane) m.source;
        ret.plane.add(p.getCallsign().toString() + ": " + m.getAsString().text);
      }
      else
        throw new ENotSupportedException();
    }
    return ret;
  }

  @Override
  public void drawStar(List<Navaid> navaidPoints) {
    for (int i = 0; i < navaidPoints.size()-1; i++) {
      p.drawLine(
          navaidPoints.get(i).getCoordinate(), 
          navaidPoints.get(i+1).getCoordinate(), 
          Color.GRAY);
    }
  }

  @Override
  public void drawApproach(Approach approach) {
    Coordinate start = Coordinates.getCoordinate(
        approach.getPoint(),
        Headings.add(approach.getRadial(), 180),
        17);
    p.drawLine(start, approach.getPoint(), Color.MAGENTA);
  }

  
}

class MessageSet{
  public final List<String> atc = new ArrayList<>();
  public final List<String> plane = new ArrayList<>();
  public final List<String> system = new ArrayList<>();
}
