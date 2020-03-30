package eng.jAtcSim.newLib.area.oldApproaches.stages.exitConditions;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.oldApproaches.stages.IExitCondition;

public class CoordinatePassedExitCondition implements IExitCondition {
  private final Coordinate coordinate;
  private final double inboundRadial;

  public CoordinatePassedExitCondition(Coordinate coordinate, double inboundRadial) {
    this.coordinate = coordinate;
    this.inboundRadial = inboundRadial;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public double getInboundRadial() {
    return inboundRadial;
  }
}
