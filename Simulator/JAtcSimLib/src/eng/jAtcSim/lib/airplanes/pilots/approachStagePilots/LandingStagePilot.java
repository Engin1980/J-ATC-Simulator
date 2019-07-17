package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.world.newApproaches.stages.LandingStage;

public class LandingStagePilot implements IApproachStagePilot<LandingStage> {
  @Override
  public eResult initStage(IPilot5Behavior pilot, LandingStage stage) {
    return null;
  }

  @Override
  public eResult flyStage(IPilot5Behavior pilot, LandingStage stage) {
    return null;
  }

  @Override
  public eResult disposeStage(IPilot5Behavior pilot, LandingStage stage) {
    return null;
  }

  @Override
  public boolean isFinishedStage(IPilot5Behavior pilot, LandingStage stage) {
    return false;
  }
}
