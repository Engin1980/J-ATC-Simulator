//package eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.geo.Coordinate;
//
//import java.util.Objects;
//
//public class XmlBorderArcPoint extends XmlBorderPoint {
//  public enum eDirection {
//    clockwise,
//    counterclockwise
//  }
//
//  private Coordinate coordinate;
//  private eDirection direction;
//
//  public XmlBorderArcPoint(Coordinate coordinate, eDirection direction) {
//    this.coordinate = coordinate;
//    this.direction = direction;
//  }
//
//  public Coordinate getCoordinate() {
//    return coordinate;
//  }
//
//  public eDirection getDirection() {
//    return direction;
//  }
//
//  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (o == null || getClass() != o.getClass()) return false;
//    XmlBorderArcPoint that = (XmlBorderArcPoint) o;
//    return Objects.equals(coordinate, that.coordinate) &&
//        direction == that.direction;
//  }
//
//  @Override
//  public int hashCode() {
//    return Objects.hash(coordinate, direction);
//  }
//}
