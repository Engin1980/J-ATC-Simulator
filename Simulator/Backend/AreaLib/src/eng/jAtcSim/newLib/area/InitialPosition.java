package eng.jAtcSim.newLib.area;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;

public class InitialPosition {

  private final Coordinate coordinate;
  private final int range;

  public static InitialPosition create(Coordinate coordinate, int range){
    return new InitialPosition(coordinate, range);
  }

  private InitialPosition(Coordinate coordinate, int range) {
    EAssert.Argument.isNotNull(coordinate);
    EAssert.Argument.isTrue(range > 0);
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
