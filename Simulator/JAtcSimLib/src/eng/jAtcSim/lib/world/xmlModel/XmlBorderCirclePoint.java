//package eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.geo.Coordinate;
//
//import java.util.Objects;
//
//public class XmlBorderCirclePoint extends XmlBorderPoint {
//  public Coordinate coordinate;
//  public double distance;
//
//  public Coordinate getCoordinate() {
//    return coordinate;
//  }
//
//  public double getDistance() {
//    return distance;
//  }
//
//  @Override
//  public int hashCode() {
//    return Objects.hash(coordinate, distance);
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
//    XmlBorderCirclePoint that = (XmlBorderCirclePoint) o;
//    return Double.compare(that.distance, distance) == 0 &&
//        Objects.equals(coordinate, that.coordinate);
//  }
//}
