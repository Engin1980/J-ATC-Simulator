package eng.jAtcSim.lib.world.approaches.stages;

import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class LandingStage implements IApproachStage {
  private final ActiveRunwayThreshold threshold;

  public LandingStage(ActiveRunwayThreshold threshold) {
    this.threshold = threshold;
  }

  public ActiveRunwayThreshold getThreshold() {
    return threshold;
  }
}
