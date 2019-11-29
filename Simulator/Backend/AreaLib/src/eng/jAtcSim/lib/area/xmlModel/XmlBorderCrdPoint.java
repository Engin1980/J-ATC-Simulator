//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.geo.Coordinate;
//
//import java.util.Objects;
//
//public class XmlBorderCrdPoint  extends XmlBorderPoint{
//  private Coordinate coordinate;
//  private int radial;
//  private double distance;
//
//  public XmlBorderCrdPoint(Coordinate coordinate, int radial, double distance) {
//    this.coordinate = coordinate;
//    this.radial = radial;
//    this.distance = distance;
//  }
//
//  public Coordinate getCoordinate() {
//    return coordinate;
//  }
//
//  public int getRadial() {
//    return radial;
//  }
//
//  public double getDistance() {
//    return distance;
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
//    XmlBorderCrdPoint that = (XmlBorderCrdPoint) o;
//    return radial == that.radial &&
//        Double.compare(that.distance, distance) == 0 &&
//        Objects.equals(coordinate, that.coordinate);
//  }
//
//  @Override
//  public int hashCode() {
//
//    return Objects.hash(coordinate, radial, distance);
//  }
//}
