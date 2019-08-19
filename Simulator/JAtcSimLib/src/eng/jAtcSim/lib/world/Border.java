/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import java.awt.geom.Line2D;
import java.util.Comparator;

/**
 * @author Marek
 */
public class Border {

  public static Border load(XElement element){
    XmlLoader.setContext(element);
    String name = XmlLoader.loadString( "name", true);
    eType type = XmlLoader.loadEnum( "type", eType.class , true);
    boolean enclosed = XmlLoader.loadBoolean("enclosed", true);
    int minAltitude = XmlLoader.loadInteger("minAltitude", true);
    int maxAltitude = XmlLoader.loadInteger("maxAltitude", true);
    Coordinate labelCoordinate = XmlLoader.loadCoordinate("labelCoordinate", false);

    IList<BorderPoint> points = new EList<>();
    for (XElement child : element.getChild("points").getChildren()) {

    }


    Border ret = new Border(name, type, points, enclosed, minAltitude, maxAltitude, labelCoordinate);
    return ret;
  }

  public enum eType {
    country,
    tma,
    ctr,
    restricted,
    danger,
    mrva,
    other
  }

//  private static final int DRAW_STEP = 10;
//  public static final int ALTITUDE_MINIMUM_VALUE = 0;
//  public static final int ALTITUDE_MAXIMUM_VALUE = 99000;
  private final String name;
  private final eType type;
  private final IList<BorderPoint> points;
  private boolean enclosed;
  private final int minAltitude;
  private final int maxAltitude;
  private final Coordinate labelCoordinate;
  private final double globalMinLng;
  private final double globalMaxLng;
  private final double globalMinLat;
  private final double globalMaxLat;

  private Border(String name, eType type, IList<BorderPoint> points, boolean enclosed, int minAltitude, int maxAltitude, Coordinate labelCoordinate) {
    this.name = name;
    this.type = type;
    this.points = points;
    this.enclosed = enclosed;
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.labelCoordinate = labelCoordinate;

    this.globalMinLat = points.minDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMaxLat = points.maxDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMinLng = points.minDouble(q -> q.getCoordinate().getLongitude().get());
    this.globalMaxLng = points.maxDouble(q -> q.getCoordinate().getLongitude().get());
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

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
    return labelCoordinate;
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
          double latMax = line.getB().getLatitude().get();
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

  public boolean isIn(double altitude) {
    boolean ret = NumberUtils.isBetween(this.minAltitude, altitude, this.maxAltitude);
    return ret;
  }

  public boolean hasIntersectionWithLine(Tuple<Coordinate, Coordinate> line) {
    boolean ret = false;
    for (int i = 0; i < getLinesCount(); i++) {
      Tuple<Coordinate, Coordinate> borderLine = getLine(i);
      ret = isLineIntersection(borderLine, line);
      if (ret) break;
    }
    return ret;
  }

  private int getLinesCount() {
    return this.points.size() - 1;
  }

  private Tuple<Coordinate, Coordinate> getLine(int index) {
    Coordinate a = this.points.get(index).getCoordinate();
    Coordinate b = this.points.get(index + 1).getCoordinate();
    Tuple<Coordinate, Coordinate> ret;
    if (a.getLongitude().get() > b.getLongitude().get()) {
      ret = new Tuple<>(b, a);
    } else {
      ret = new Tuple(a, b);
    }
    return ret;
  }

  private boolean isLineIntersection(Tuple<Coordinate, Coordinate> a, Tuple<Coordinate, Coordinate> b) {
    boolean ret = Line2D.linesIntersect(
        a.getA().getLatitude().get(), a.getA().getLongitude().get(),
        a.getB().getLatitude().get(), a.getB().getLongitude().get(),
        b.getA().getLatitude().get(), b.getA().getLongitude().get(),
        b.getB().getLatitude().get(), b.getB().getLongitude().get());
    return ret;
  }
}
