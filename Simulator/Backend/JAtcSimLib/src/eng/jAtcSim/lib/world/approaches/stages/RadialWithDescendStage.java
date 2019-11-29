package eng.jAtcSim.lib.world.approaches.stages;

import eng.eSystem.geo.Coordinate;

public class RadialWithDescendStage extends RadialStage {
  private final double altitude;
  private final double slope;

  public RadialWithDescendStage(
      Coordinate radialReferencePoint, int inboundRadial,double altitude, double slope,
      IExitCondition exitCondition) {
    super(radialReferencePoint, inboundRadial, exitCondition);
    this.altitude = altitude;
    this.slope = slope;
  }
}
