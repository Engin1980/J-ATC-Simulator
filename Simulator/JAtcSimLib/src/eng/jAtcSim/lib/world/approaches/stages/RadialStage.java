package eng.jAtcSim.lib.world.approaches.stages;

import eng.eSystem.geo.Coordinate;

public class RadialStage implements IApproachStage {
  private Coordinate fix;
  private int inboundRadial;
  private IExitCondition exitCondition;

  public IExitCondition getExitCondition() {
    return exitCondition;
  }

  public Coordinate getFix() {
    return fix;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }
}
