package eng.jAtcSim.lib.world.approaches.stages.exitConditions;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.world.approaches.stages.IExitCondition;

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
