package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.contextLocal.Context;

public class FlyRadialBehavior implements IApproachBehavior {
  private final Coordinate coordinate;
  private final double inboundRadialWithDeclination;

  protected FlyRadialBehavior(Coordinate coordinate, double inboundRadialWithDeclination) {
    EAssert.Argument.isNotNull(coordinate, "coordinate");
    this.coordinate = coordinate;
    this.inboundRadialWithDeclination = inboundRadialWithDeclination;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getInboundRadialWithDeclination() {
    return inboundRadialWithDeclination;
  }
}
