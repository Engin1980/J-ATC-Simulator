package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.eSystem.geo.Coordinate;

public class RadialStage implements IApproachStage {
  private Coordinate fix;
  private int inboundRadial;
  private IRadialStageExitCondition exitCondition;
}
