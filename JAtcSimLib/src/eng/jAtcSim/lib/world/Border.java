/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import sun.font.FontRunIterator;

import java.awt.geom.Line2D;

/**
 * @author Marek
 */
public class Border {
  public enum eType {
    country,
    tma,
    ctr,
    restricted,
    mrva,
    other
  }

  private static final int DRAW_STEP = 10;
  private String name;
  private eType type;
  private IList<BorderPoint> points;
  private boolean enclosed;
  @XmlOptional
  private int minAltitude = 0;
  @XmlOptional
  private int maxAltitude = 99000;
  @XmlOptional
  private Coordinate labelCoordinate;
  @XmlIgnore
  private double globalMinLng;
  @XmlIgnore
  private double globalMaxLng;
  @XmlIgnore
  private double globalMinLat;
  @XmlIgnore
  private double globalMaxLat;
  @XmlIgnore
  private IList<BorderExactPoint> exactPoints;

  public IList<BorderExactPoint> getExactPoints() {
    return exactPoints;
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  @Deprecated
  public IList<BorderPoint> getPoints() {
    return points;
  }

  public boolean isEnclosed() {
    return enclosed;
  }

  public int getMinAltitude() {
    return minAltitude;
  }

  public int getMaxAltitude() {
    return maxAltitude;
  }

  public Coordinate getLabelCoordinate() {
    if (labelCoordinate == null) {
      generateLabelCoordinate();
    }
    return labelCoordinate;
  }

  public void bind() {
    expandArcsToPoints();

    this.globalMinLat = exactPoints.min(q -> q.getCoordinate().getLatitude().get());
    this.globalMaxLat = exactPoints.max(q -> q.getCoordinate().getLatitude().get());
    this.globalMinLng = exactPoints.min(q -> q.getCoordinate().getLongitude().get());
    this.globalMaxLng = exactPoints.max(q -> q.getCoordinate().getLongitude().get());
  }

  private void generateLabelCoordinate() {
    IList<BorderExactPoint> tmp = points.where(q -> q instanceof BorderExactPoint).select(q -> (BorderExactPoint) q);
    double latMin = tmp.min(q -> q.getCoordinate().getLatitude().get());
    double latMax = tmp.max(q -> q.getCoordinate().getLatitude().get());
    double lngMin = tmp.min(q -> q.getCoordinate().getLongitude().get());
    double lngMax = tmp.max(q -> q.getCoordinate().getLongitude().get());

    double lat = (latMax + latMin) / 2;
    double lng = (lngMax + lngMin) / 2;

    this.labelCoordinate = new Coordinate(lat, lng);
  }

  private void expandArcsToPoints() {
    if (this.isEnclosed() && !this.points.get(0).equals(this.points.get(this.points.size() - 1)))
      this.points.add(this.points.get(0));

    this.exactPoints = new EList<>();

    for (int i = 0; i < this.points.size(); i++) {
      if (this.points.get(i) instanceof BorderExactPoint)
        this.exactPoints.add((BorderExactPoint) this.points.get(i));
      else {
        BorderExactPoint prev = (BorderExactPoint) this.points.get(i - 1);
        BorderArcPoint curr = (BorderArcPoint) this.points.get(i);
        BorderExactPoint next = (BorderExactPoint) this.points.get(i + 1);
        IList<BorderExactPoint> tmp = generateArcPoints(prev, curr, next);
        this.exactPoints.add(tmp);
      }
    }
  }

  private IList<BorderExactPoint> generateArcPoints(BorderExactPoint prev, BorderArcPoint curr, BorderExactPoint next) {
    IList<BorderExactPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(curr.getCoordinate(), prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(curr.getCoordinate(), next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(curr.getCoordinate(), prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(curr.getCoordinate(), next.getCoordinate())) / 2;
    double step;
    if (curr.getDirection() == BorderArcPoint.eDirection.clockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else if (curr.getDirection() == BorderArcPoint.eDirection.counterclockwise) {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    } else {
      throw new UnsupportedOperationException("This combination is not supported.");
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(curr.getCoordinate(), pt, dist);
        BorderExactPoint p = new BorderExactPoint(c);
        ret.add(p);
      }
    }

    return ret;
  }

  public boolean isIn(Coordinate c) {
    boolean ret = NumberUtils.isBetweenOrEqual(globalMinLng, c.getLongitude().get(), globalMaxLng);
    if (ret)
      ret = NumberUtils.isBetweenOrEqual(globalMinLat, c.getLatitude().get(), globalMaxLat);
    if (ret) {
      int hit = 0;
      for (int i = 0; i < getLinesCount(); i++) {
        Tuple<Coordinate, Coordinate> line = getLine(i);
        if (line.getB().getLongitude().get() < c.getLongitude().get()) {
          // line longitude on the left side
          continue;
        } else if (line.getA().getLongitude().get() > c.getLongitude().get()) {
          // line longitude on the right side
          double latMin = line.getA().getLatitude().get();
          double latMax = line.getA().getLatitude().get();
          if (latMin > latMax) {
            double tmp = latMin;
            latMin = latMax;
            latMax = tmp;
          }
          if (NumberUtils.isBetweenOrEqual(latMin, c.getLatitude().get(), latMax)) hit++;
        } else {
          // line longitude in range
          if (!NumberUtils.isInRange(line.getA().getLatitude().get(), c.getLatitude().get(), line.getB().getLatitude().get()))
            continue;
          double a = (line.getB().getLatitude().get() - line.getA().getLatitude().get()) / (line.getB().getLongitude().get() - line.getA().getLongitude().get());
          double b = line.getA().getLatitude().get() - a * line.getA().getLongitude().get();
          double p = a * c.getLongitude().get() + b;
          double diff = c.getLatitude().get() - p;
          if (a >= 0 && diff > 0)
            hit++;
          else if (a < 0 && diff < 0)
            hit++;
        }
      }
      ret = (hit % 2 == 1);
    }

    return ret;
  }

  private int getLinesCount() {
    return this.exactPoints.size() - 1;
  }

  private Tuple<Coordinate, Coordinate> getLine(int index) {
    Tuple<Coordinate, Coordinate> ret = new Tuple<>(
        this.exactPoints.get(index).getCoordinate(),
        this.exactPoints.get(index + 1).getCoordinate()
    );
    return ret;
  }

  public boolean isIn(double altitude) {
    boolean ret = NumberUtils.isBetween(this.minAltitude, altitude, this.maxAltitude);
    return ret;
  }

  public boolean hasIntersectionWithLine(Tuple<Coordinate, Coordinate> line){
   boolean ret = false;
    for (int i = 0; i < getLinesCount(); i++) {
      Tuple<Coordinate, Coordinate> borderLine = getLine(i);
      ret = isLineIntersection(borderLine, line);
      if (ret) break;
    }
    return ret;
  }

  private boolean isLineIntersection(Tuple<Coordinate,Coordinate> a, Tuple<Coordinate,Coordinate> b) {
    boolean ret = Line2D.linesIntersect(
        a.getA().getLatitude().get(), a.getA().getLongitude().get(),
        a.getB().getLatitude().get(), a.getB().getLongitude().get(),
        b.getA().getLatitude().get(), b.getA().getLongitude().get(),
        b.getB().getLatitude().get(), b.getB().getLongitude().get());
    return ret;
  }
}
