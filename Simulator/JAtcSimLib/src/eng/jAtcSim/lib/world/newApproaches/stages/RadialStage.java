package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.eSystem.geo.Coordinate;

public class RadialStage implements IApproachStage {
  private Coordinate fix;
  private int inboundRadial;
  private IRadialStageExitCondition exitCondition;

  public IRadialStageExitCondition getExitCondition() {
    return exitCondition;
  }

  public Coordinate getFix() {
    return fix;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }
}
