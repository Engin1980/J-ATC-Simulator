package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.world.newApproaches.stages.RadialStage;

public class RadialStagePilot implements IApproachStagePilot<RadialStage> {
  @Override
  public eResult initStage(IPilot4Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public eResult disposeStage(IPilot4Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot, RadialStage stage) {
    return false;
  }
}
