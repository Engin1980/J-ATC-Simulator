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
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.xml.XmlLoader;

import java.awt.geom.Line2D;
import java.util.Comparator;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class Border {

  public static class ByDisjointsComparator implements Comparator<Border> {

    @Override
    public int compare(Border a, Border b) {
      if (a.disjoints.contains(b.getName()))
        if (b.disjoints.contains(a.getName()))
          throw new EApplicationException("Borders has cyclic dependency in disjoints definition. Borders. " + a.getName() + ", " + b.getName());
        else
          return 1;
      else if (b.disjoints.contains(a.getName()))
        return -1;
      else
        return 0;
    }
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

  public static Border load(XElement source, IReadOnlyList<Navaid> navaids) {
    XmlLoader.setContext(source);
    String name = XmlLoader.loadString("name");
    eType type = XmlLoader.loadEnum("type", eType.class);
    boolean enclosed = XmlLoader.loadBoolean("enclosed");
    int minAltitude = XmlLoader.loadInteger("minAltitude");
    int maxAltitude = XmlLoader.loadInteger("maxAltitude");
    Coordinate labelCoordinate = XmlLoader.loadCoordinate("labelCoordinate", null);

    IReadOnlyList<XElement> pointElements = source.getChild("points").getChildren();
    IList<BorderPoint> points;
    if (pointElements.size() == 1 && pointElements.get(0).getName().equals("circle"))
      points = loadPointsFromCircle(pointElements.get(0));
    else
      points = loadPoints(source.getChild("points").getChildren(), navaids);

    IList<String> disjoints = new EList<>();
    source.getChild("disjoints").getChildren().forEach(q -> disjoints.add(q.getContent()));

    Border ret = new Border(name, type, points, enclosed, minAltitude, maxAltitude, disjoints, labelCoordinate);
    return ret;
  }

  public static IList<Border> loadList(IReadOnlyList<XElement> sources, IReadOnlyList<Navaid> navaids) {
    IList<Border> ret = new EList<>();
    for (XElement child : sources) {
      Border border = Border.load(child, navaids);
      ret.add(border);
    }
    ret.sort(new Border.ByDisjointsComparator());
    return ret;
  }

  private static IList<BorderPoint> loadPointsFromCircle(XElement source) {
    IList<BorderPoint> ret = new EList<>();
    Coordinate coord = XmlLoader.loadCoordinate(source, "coordinate");
    double dist = XmlLoader.loadDouble(source, "distance");
    Coordinate pointCoordinate = Coordinates.getCoordinate(coord, 0, dist);
    BorderPoint point = BorderPoint.create(pointCoordinate);
    IList<BorderPoint> tmp = generateArcPoints(point, coord, true, point);
    ret.add(point);
    ret.add(tmp);
    return ret;
  }

  private static IList<BorderPoint> loadPoints(IReadOnlyList<XElement> nodes, IReadOnlyList<Navaid> navaids) {
    IList<BorderPoint> ret = new EList<>();
    IList<Tuple<Integer, XElement>> arcTuples = new EList<>();
    BorderPoint point;

    for (XElement node : nodes) {
      switch (node.getName()) {
        case "point":
          point = BorderPoint.load(node);
          ret.add(point);
          break;
        case "arc":
          arcTuples.add(new Tuple<>(ret.size(), node));
          break;
        case "crd":
          Coordinate coordinate = XmlLoader.loadCoordinate(node, "coordinate");
          int radial = XmlLoader.loadInteger(node, "radial");
          double distance = XmlLoader.loadDouble(node, "distance");
          Coordinate borderPointCoordinate = Coordinates.getCoordinate(
              coordinate, radial, distance);
          point = BorderPoint.create(borderPointCoordinate);
          ret.add(point);
          break;
        default:
          throw new EApplicationException(sf("Unknown type of point '%s' in border.", node.getName()));
      }
    }

    for (Tuple<Integer, XElement> arcTuple : arcTuples) {
      int index = arcTuple.getA();
      Coordinate coordinate = XmlLoader.loadCoordinate(arcTuple.getB(), "coordinate");
      boolean isClockwise = XmlLoader.loadStringRestricted(arcTuple.getB(), "direction",
          new String[]{"clockwise", "counterclockwise"}).equals("clockwise");
      IList<BorderPoint> arcPoints = generateArcPoints(
          ret.get(index - 1), coordinate, isClockwise, ret.get(index));
      ret.insert(index, arcPoints);
    }

    return ret;
  }

  private static IList<BorderPoint> generateArcPoints(BorderPoint prev, Coordinate currCoordinate, boolean isClockwise, BorderPoint next) {
    final int BORDER_ARC_POINT_DRAW_STEP = 10;
    IList<BorderPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(currCoordinate, prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(currCoordinate, next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(currCoordinate, prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(currCoordinate, next.getCoordinate())) / 2;
    double step;
    if (isClockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % BORDER_ARC_POINT_DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(currCoordinate, pt, dist);
        BorderPoint p = BorderPoint.create(c);
        ret.add(p);
      }
    }

    return ret;
  }

  //  private static final int DRAW_STEP = 10;
//  public static final int ALTITUDE_MINIMUM_VALUE = 0;
//  public static final int ALTITUDE_MAXIMUM_VALUE = 99000;
  private final String name;
  private final eType type;
  private final IList<BorderPoint> points;
  private final int minAltitude;
  private final int maxAltitude;
  private final Coordinate labelCoordinate;
  private final double globalMinLng;
  private final double globalMaxLng;
  private final double globalMinLat;
  private final double globalMaxLat;
  private boolean enclosed;
  private IList<String> disjoints;

  private Border(String name, eType type, IList<BorderPoint> points,
                 boolean enclosed, int minAltitude, int maxAltitude, IList<String> disjoints,
                 Coordinate labelCoordinate) {
    this.name = name;
    this.type = type;
    this.points = points;
    this.enclosed = enclosed;
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.labelCoordinate = labelCoordinate;
    this.disjoints = disjoints;

    this.globalMinLat = points.minDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMaxLat = points.maxDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMinLng = points.minDouble(q -> q.getCoordinate().getLongitude().get());
    this.globalMaxLng = points.maxDouble(q -> q.getCoordinate().getLongitude().get());
  }

  public Coordinate getLabelCoordinate() {
    return labelCoordinate;
  }

  public int getMaxAltitude() {
    return maxAltitude;
  }

  public int getMinAltitude() {
    return minAltitude;
  }

  public String getName() {
    return name;
  }

  public IList<BorderPoint> getPoints() {
    return points;
  }

  public eType getType() {
    return type;
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

  public boolean isEnclosed() {
    return enclosed;
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

  private int getLinesCount() {
    return this.points.size() - 1;
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
