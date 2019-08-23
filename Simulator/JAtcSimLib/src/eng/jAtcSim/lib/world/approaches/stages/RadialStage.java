package eng.jAtcSim.lib.world.approaches.stages;

import eng.eSystem.geo.Coordinate;

public class RadialStage implements IApproachStage {
  private Coordinate coordinate;
  private int inboundRadial;
  private IExitCondition exitCondition;

  public RadialStage(Coordinate coordinate, int inboundRadial, IExitCondition exitCondition) {
    assert coordinate != null;
    assert exitCondition != null;
    this.coordinate = coordinate;
    this.inboundRadial = inboundRadial;
    this.exitCondition = exitCondition;
  }

  public IExitCondition getExitCondition() {
    return exitCondition;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }
}
