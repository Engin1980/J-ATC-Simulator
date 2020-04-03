package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;

public class FlyRadialBehavior implements IApproachBehavior {
  private final Coordinate coordinate;
  private final int inboundRadial;

  protected FlyRadialBehavior(Coordinate coordinate, int inboundRadial) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    this.coordinate = coordinate;
    this.inboundRadial = inboundRadial;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }
}
