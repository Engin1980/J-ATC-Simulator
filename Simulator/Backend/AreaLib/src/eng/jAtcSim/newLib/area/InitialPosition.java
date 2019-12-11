package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class InitialPosition {

  public static InitialPosition load(XElement source) {
    XmlLoader.setContext(source);
    Coordinate coordinate = XmlLoader.loadCoordinate("coordinate");
    int range = XmlLoader.loadInteger("range");
    InitialPosition ret = new InitialPosition(coordinate, range);
    return ret;
  }

  private final Coordinate coordinate;
  private final int range;

  private InitialPosition(Coordinate coordinate, int range) {
    this.coordinate = coordinate;
    this.range = range;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getRange() {
    return range;
  }
}
