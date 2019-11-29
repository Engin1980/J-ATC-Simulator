//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.exceptions.EApplicationException;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
//import eng.jAtcSim.lib.eng.jAtcSim.lib.world.Border;
//
//import java.util.Comparator;
//
//public class XmlBorder {
//
//  public static class ByDisjointsComparator implements Comparator<XmlBorder> {
//
//    @Override
//    public int compare(XmlBorder a, XmlBorder b) {
//      if (a.getDisjoints().contains(b.getName()))
//        if (b.getDisjoints().contains(a.getName()))
//          throw new EApplicationException("Borders has cyclic dependency in disjoints definition. Borders. " + a.getName() + ", " + b.getName());
//        else
//          return 1;
//      else if (b.getDisjoints().contains(a.getName()))
//        return -1;
//      else
//        return 0;
//    }
//  }
//
//  public static final int ALTITUDE_MINIMUM_VALUE = 0;
//  public static final int ALTITUDE_MAXIMUM_VALUE = 99000;
//  public String name;
//  public Border.eType type;
//  @XmlItemElement(elementName = "point", type = XmlBorderExactPoint.class)
//  @XmlItemElement(elementName = "arc", type = XmlBorderArcPoint.class)
//  @XmlItemElement(elementName = "crd", type = XmlBorderCrdPoint.class)
//  @XmlItemElement(elementName = "circle", type = XmlBorderCirclePoint.class)
//  public IList<XmlBorderPoint> points;
//  public boolean enclosed;
//  @XmlOptional
//  public int minAltitude = ALTITUDE_MINIMUM_VALUE;
//  @XmlOptional
//  public int maxAltitude = ALTITUDE_MAXIMUM_VALUE;
//  @XmlOptional
//  public Coordinate labelCoordinate;
//
//  @XmlOptional
//  public IList<String> disjoints = new EList<>();
//
//  public String getName() {
//    return name;
//  }
//
//  public Border.eType getType() {
//    return type;
//  }
//
//  public IReadOnlyList<String> getDisjoints() {
//    return disjoints;
//  }
//
//  public IList<XmlBorderPoint> getPoints() {
//    return points;
//  }
//
//  public boolean isEnclosed() {
//    return enclosed;
//  }
//
//  public int getMinAltitude() {
//    return minAltitude;
//  }
//
//  public int getMaxAltitude() {
//    return maxAltitude;
//  }
//
//  public Coordinate getLabelCoordinate() {
//    return labelCoordinate;
//  }
//
////  public boolean isIn(Coordinate c) {
////    boolean ret = NumberUtils.isBetweenOrEqual(globalMinLng, c.getLongitude().get(), globalMaxLng);
////    if (ret)
////      ret = NumberUtils.isBetweenOrEqual(globalMinLat, c.getLatitude().get(), globalMaxLat);
////    if (ret) {
////      int hit = 0;
////      for (int i = 0; i < getLinesCount(); i++) {
////        Tuple<Coordinate, Coordinate> line = getLine(i);
////        if (line.getB().getLongitude().get() < c.getLongitude().get()) {
////          // line longitude on the left side
////          continue;
////        } else if (line.getA().getLongitude().get() > c.getLongitude().get()) {
////          // line longitude on the right side
////          double latMin = line.getA().getLatitude().get();
////          double latMax = line.getB().getLatitude().get();
////          if (latMin > latMax) {
////            double tmp = latMin;
////            latMin = latMax;
////            latMax = tmp;
////          }
////          if (NumberUtils.isBetweenOrEqual(latMin, c.getLatitude().get(), latMax)) hit++;
////        } else {
////          // line longitude in range
////          if (!NumberUtils.isInRange(line.getA().getLatitude().get(), c.getLatitude().get(), line.getB().getLatitude().get()))
////            continue;
////          double a = (line.getB().getLatitude().get() - line.getA().getLatitude().get()) / (line.getB().getLongitude().get() - line.getA().getLongitude().get());
////          double b = line.getA().getLatitude().get() - a * line.getA().getLongitude().get();
////          double p = a * c.getLongitude().get() + b;
////          double diff = c.getLatitude().get() - p;
////          if (a >= 0 && diff > 0)
////            hit++;
////          else if (a < 0 && diff < 0)
////            hit++;
////        }
////      }
////      ret = (hit % 2 == 1);
////    }
////
////    return ret;
////  }
////
////  public boolean isIn(double altitude) {
////    boolean ret = NumberUtils.isBetween(this.minAltitude, altitude, this.maxAltitude);
////    return ret;
////  }
////
////  public boolean hasIntersectionWithLine(Tuple<Coordinate, Coordinate> line) {
////    boolean ret = false;
////    for (int i = 0; i < getLinesCount(); i++) {
////      Tuple<Coordinate, Coordinate> borderLine = getLine(i);
////      ret = isLineIntersection(borderLine, line);
////      if (ret) break;
////    }
////    return ret;
////  }
////
////  private void generateLabelCoordinate() {
////    IList<BorderExactPoint> tmp = points.where(q -> q instanceof BorderExactPoint).select(q -> (BorderExactPoint) q);
////    double latMin = tmp.minDouble(q -> q.getCoordinate().getLatitude().get());
////    double latMax = tmp.maxDouble(q -> q.getCoordinate().getLatitude().get());
////    double lngMin = tmp.minDouble(q -> q.getCoordinate().getLongitude().get());
////    double lngMax = tmp.maxDouble(q -> q.getCoordinate().getLongitude().get());
////
////    double lat = (latMax + latMin) / 2;
////    double lng = (lngMax + lngMin) / 2;
////
////    this.labelCoordinate = new Coordinate(lat, lng);
////  }
//
//}
