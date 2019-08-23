package eng.jAtcSim.lib.world.approaches.stages;

import eng.eSystem.geo.Coordinate;

public class FlyToPointStage implements IApproachStage {
  private final Coordinate coordinate;
  private final IExitCondition exitCondition;

  public FlyToPointStage(Coordinate coordinate, IExitCondition exitCondition) {
    this.coordinate = coordinate;
    this.exitCondition = exitCondition;
  }

  public IExitCondition getExitCondition() {
    return exitCondition;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }
}
