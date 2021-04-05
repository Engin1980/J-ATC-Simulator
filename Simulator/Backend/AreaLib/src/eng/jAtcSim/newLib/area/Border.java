package eng.jAtcSim.newLib.area;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.utilites.NumberUtils;

import java.util.Comparator;

public class Border {

  public enum eType {
    country,
    tma,
    ctr,
    restricted,
    danger,
    mrva,
    other
  }

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

  public static Border create(String name, Border.eType type, boolean enclosed,
                              int minAltitude, int maxAltitude, Coordinate labelCoordinate,
                              IList<BorderPoint> points,
                              IList<String> disjoints) {
    Border ret = new Border();
    ret.name = name;
    ret.type = type;
    ret.points = points;
    ret.minAltitude = minAltitude;
    ret.maxAltitude = maxAltitude;
    ret.labelCoordinate = labelCoordinate;
    ret.enclosed = enclosed;
    ret.disjoints = disjoints;


    if (ret.labelCoordinate == null)
      ret.labelCoordinate = generateLabelCoordinate(ret.points);
    ret.updateBoundingBox();

    return ret;
  }

  //TODO to instance
  private static Coordinate generateLabelCoordinate(IList<BorderPoint> points) {
    double latMin = points.minDouble(q -> q.getCoordinate().getLatitude().get()).orElseThrow();
    double latMax = points.maxDouble(q -> q.getCoordinate().getLatitude().get()).orElseThrow();
    double lngMin = points.minDouble(q -> q.getCoordinate().getLongitude().get()).orElseThrow();
    double lngMax = points.maxDouble(q -> q.getCoordinate().getLongitude().get()).orElseThrow();

    double lat = (latMax + latMin) / 2;
    double lng = (lngMax + lngMin) / 2;

    return new Coordinate(lat, lng);
  }
  private String name;
  private eType type;
  private IList<BorderPoint> points;
  private int minAltitude;
  private int maxAltitude;
  private Coordinate labelCoordinate;
  private double globalMinLng;
  private double globalMaxLng;
  private double globalMinLat;
  private double globalMaxLat;
  private boolean enclosed;
  private IList<String> disjoints;

  private Border() {
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
    boolean ret = LineUtils.linesIntersect(
            a.getA().getLatitude().get(), a.getA().getLongitude().get(),
            a.getB().getLatitude().get(), a.getB().getLongitude().get(),
            b.getA().getLatitude().get(), b.getA().getLongitude().get(),
            b.getB().getLatitude().get(), b.getB().getLongitude().get());
    return ret;
  }

  private void updateBoundingBox() {
    this.globalMinLat = points.minDouble(q -> q.getCoordinate().getLatitude().get()).orElseThrow();
    this.globalMaxLat = points.maxDouble(q -> q.getCoordinate().getLatitude().get()).orElseThrow();
    this.globalMinLng = points.minDouble(q -> q.getCoordinate().getLongitude().get()).orElseThrow();
    this.globalMaxLng = points.maxDouble(q -> q.getCoordinate().getLongitude().get()).orElseThrow();
  }
}
