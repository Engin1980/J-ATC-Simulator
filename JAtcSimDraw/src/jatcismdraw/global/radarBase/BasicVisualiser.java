/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcismdraw.global.radarBase;

import jatcsimdraw.mainRadar.settings.DispItem;
import jatcsimdraw.mainRadar.settings.DispPlane;
import jatcsimdraw.mainRadar.settings.DispText;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.atcs.Atc;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.ETime;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marek
 */
public class BasicVisualiser extends Visualiser {

  /**
   * Remembers last repaint of "hour" on the visualiser. 
   * Hour change increases refreshTick variable
   */
  private static int lastDrawnTimeTotalSeconds;
  /**
   * RefreshTick is used to define how often history of planes should be stored.
   * This value should be increased every second/and repaint.
   */
  private static long refreshTick = 0;
  private final PlaneHistoryDotManager planeDotHistory = new PlaneHistoryDotManager();

  public BasicVisualiser(Painter p, Settings sett) {
    super(p, sett);
  }

  @Override
  public void drawMessages(List<Message> msgs) {
    MessageSet ms = createMessageSet(msgs);

    DispText dt;

    dt = sett.getDispText(DispText.eType.atc);
    p.drawTextBlock(ms.atc, Painter.eTextBlockLocation.bottomRight, dt.getFont(), dt.getColor());

    dt = sett.getDispText(DispText.eType.plane);
    p.drawTextBlock(ms.plane, Painter.eTextBlockLocation.bottomLeft, dt.getFont(), dt.getColor());

    dt = sett.getDispText(DispText.eType.system);
    p.drawTextBlock(decodeSystemMultilines(ms.system), Painter.eTextBlockLocation.topRight, dt.getFont(), dt.getColor());
  }

  @Override
  public void drawBorder(Border border) {

    DispItem ds = getDispSettBy(border);

    Coordinate last = null;
    for (int i = 0; i < border.getPoints().size(); i++) {
      BorderPoint bp = border.getPoints().get(i);
      if (bp instanceof BorderExactPoint) {
        BorderExactPoint bep = (BorderExactPoint) bp;
        if (last != null) {
          p.drawLine(last, bep.getCoordinate(), ds.getColor(), ds.getWidth());
        }
        last = bep.getCoordinate();
      } else if (bp instanceof BorderArcPoint) {
        BorderExactPoint bPrev = (BorderExactPoint) border.getPoints().get(i - 1);
        BorderExactPoint bNext = (BorderExactPoint) border.getPoints().get(i + 1);
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
    DispText dt = sett.getDispText(DispText.eType.navaid);

    switch (navaid.getType()) {
      case VOR:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawCircleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case NDB:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawTriangleAround(navaid.getCoordinate(), ds.getBorderDistance(), ds.getColor(), ds.getBorderWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
        break;
      case Fix:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        //p.drawCircleAround(navaid.getCoordinate(), 9, ds.getColor(), 1);
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 0, dt.getFont(), ds.getColor());
        break;
      case FixMinor:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        break;
      case Airport:
        p.drawPoint(navaid.getCoordinate(), ds.getColor(), ds.getWidth());
        p.drawText(navaid.getName(), navaid.getCoordinate(), 3, 3, dt.getFont(), ds.getColor());
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
    if (borderArcPoint.getDirection() == BorderArcPoint.eDirection.counterclockwise) {
      double tmp = startBear;
      startBear = endBear;
      endBear = tmp;
    }

    p.drawArc(borderArcPoint.getCoordinate(), startBear, endBear, distance, color);
  }

  @Override
  public void drawPlane(Airplane.AirplaneInfo planeInfo) {

    DispPlane dp = sett.getDispPlane(planeInfo);
    DispText dt = sett.getDispText(DispText.eType.callsign);
    Color c = dp.getColor();
    if (planeInfo.isAirprox()) {
      c = Color.RED;
    }

    if (dp.isVisible() == false) {
      return;
    }

    // plane dot and direction line
    p.drawPoint(planeInfo.coordinate(), c, dp.getPointWidth()); // point of plane
    p.drawLineByHeadingAndDistance(planeInfo.coordinate(), planeInfo.heading(), dp.getHeadingLineLength(), c, 1);

    // separation ring
    if (planeInfo.speed() > 100 || planeInfo.verticalSpeed() != 0) {
      p.drawCircleAroundInNM(planeInfo.coordinate(), dp.getSeparationRingRadius(),
        c, 1);
    }

    // plane label
    StringBuilder sb = new StringBuilder();
    sb.append(
      buildPlaneString(dp.getFirstLineFormat(), planeInfo));

    if (planeInfo.tunedAtc() != planeInfo.responsibleAtc()) {
      sb.append("*");
    }

    sb.append("\r\n");
    sb.append(
      buildPlaneString(dp.getSecondLineFormat(), planeInfo));
    sb.append("\r\n");
    sb.append(
      buildPlaneString(dp.getThirdLineFormat(), planeInfo));

    p.drawText(sb.toString(), planeInfo.coordinate(), 3, 3, dt.getFont(), c);

    // plane history
    if (refreshTick % dp.getHistoryDotStep() == 0) {
      this.planeDotHistory.add(planeInfo.callsign(), planeInfo.coordinate(), dp.getHistoryDotCount());
    }
    List<Coordinate> hist = planeDotHistory.get(planeInfo.callsign());
    if (hist != null) {
      for (Coordinate coordinate : hist) {
        p.drawPoint(coordinate, c, 3);
      }
    }
  }

  private String buildPlaneString(String lineFormat, Airplane.AirplaneInfo planeInfo) {
    String ret = planeInfo.format(lineFormat);
    return ret;
  }

  private MessageSet createMessageSet(List<Message> msgs) {
    MessageSet ret = new MessageSet();

    for (Message m : msgs) {
      if (m.isFromSystemMessage()) {
        ret.system.add(">> " + m.getAsString().text);
      } else if (m.isFromAtcMessage()) {
        Atc atc = (Atc) m.source;
        ret.atc.add(atc.getName() + ": " + m.toContentString());
      } else if (m.isFromPlaneMessage()) {
        Airplane p = (Airplane) m.source;
        ret.plane.add(p.getCallsign().toString() + ": " + m.toContentString());
      } else {
        throw new ENotSupportedException();
      }
    }
    return ret;
  }

  @Override
  public void drawStar(List<Navaid> navaidPoints) {
    DispItem di = sett.getDispItem(DispItem.STAR);
    for (int i = 0; i < navaidPoints.size() - 1; i++) {
      p.drawLine(
        navaidPoints.get(i).getCoordinate(),
        navaidPoints.get(i + 1).getCoordinate(),
        di.getColor());
    }
  }

  @Override
  public void drawSid(List<Navaid> navaidPoints) {
    DispItem di = sett.getDispItem(DispItem.SID);
    for (int i = 0; i < navaidPoints.size() - 1; i++) {
      p.drawLine(
        navaidPoints.get(i).getCoordinate(),
        navaidPoints.get(i + 1).getCoordinate(),
        di.getColor());
    }
  }

  @Override
  public void drawApproach(Approach approach) {
    Coordinate start = Coordinates.getCoordinate(
      approach.getPoint(),
      Headings.add(approach.getRadial(), 180),
      17);
    p.drawLine(start, approach.getPoint(), Color.MAGENTA);
    if (approach.getParent().getFafCross() != null){
      p.drawCross(approach.getParent().getFafCross(), Color.MAGENTA, 5, 1);  
    }
    
  }

  @Override
  public void afterDraw() {
    super.afterDraw();
    this.planeDotHistory.removeOneHistoryFromAll();
  }

  @Override
  public void drawTime(ETime time) {
    DispText dt = sett.getDispText(DispText.eType.time);
    List<String> lst = new ArrayList();
    lst.add(time.toString());
    p.drawTextBlock(lst, Painter.eTextBlockLocation.topLeft, dt.getFont(), dt.getColor());

    if (lastDrawnTimeTotalSeconds != time.getTotalSeconds()){
      lastDrawnTimeTotalSeconds = time.getTotalSeconds();
      refreshTick++;
    }
  }

  private List<String> decodeSystemMultilines(List<String> system) {
    List<String> ret = new ArrayList<>();
    String del = "\r\n";
    for (String s : system) {
      String[] spl = s.split(del);
      ret.addAll(Arrays.asList(spl));
    }
    return ret;
  }
}

class MessageSet {

  public final List<String> atc = new ArrayList<>();
  public final List<String> plane = new ArrayList<>();
  public final List<String> system = new ArrayList<>();
}

class PlaneHistoryDotManager {

  private final Map<Callsign, List<Coordinate>> inner = new HashMap<>();
  private final Map<Callsign, Integer> maxCount = new HashMap<>();

  public void add(Callsign cs, Coordinate c, int maxHistory) {
    if (inner.containsKey(cs) == false) {
      inner.put(cs, new LinkedList<Coordinate>());
      maxCount.put(cs, maxHistory);
    }

    inner.get(cs).add(c);
    if (maxCount.get(cs) != maxHistory) {
      maxCount.put(cs, maxHistory);
    }
  }

  public void removeOneHistoryFromAll() {
    List<Callsign> tr = new LinkedList<>();
    for (Callsign cs : inner.keySet()) {
      List<Coordinate> l = inner.get(cs);
      if (l.size() > maxCount.get(cs)) {
        l.remove(0);
      }
      if (l.isEmpty()) {
        tr.add(cs);
      }
    }

    for (Callsign cs : tr) {
      inner.remove(cs);
      maxCount.remove(cs);
    }
  }

  List<Coordinate> get(Callsign callsign) {
    return inner.get(callsign);
  }
}
