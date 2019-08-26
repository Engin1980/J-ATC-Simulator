//package eng.jAtcSim.lib.world.xmlModel;
//
//import eng.eSystem.geo.Coordinate;
//import eng.jAtcSim.lib.world.BorderPoint;
//
//import java.util.Objects;
//
//public class XmlBorderExactPoint extends XmlBorderPoint {
//  private Coordinate coordinate;
//
//  public XmlBorderExactPoint() {
//  }
//
//  public XmlBorderExactPoint(Coordinate coordinate) {
//    this.coordinate = coordinate;
//  }
//
//  public Coordinate getCoordinate() {
//    return coordinate;
//  }
//
//  @Override
//  public int hashCode() {
//    int hash = 7;
//    hash = 97 * hash + Objects.hashCode(this.coordinate);
//    return hash;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (obj == null) {
//      return false;
//    }
//    if (getClass() != obj.getClass()) {
//      return false;
//    }
//    final XmlBorderExactPoint other = (XmlBorderExactPoint) obj;
//    if (!Objects.equals(this.coordinate, other.coordinate)) {
//      return false;
//    }
//    return true;
//  }
//}
