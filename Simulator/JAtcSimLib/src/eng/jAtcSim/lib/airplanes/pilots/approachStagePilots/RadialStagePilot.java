package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;
import eng.jAtcSim.lib.world.newApproaches.stages.RadialStage;

public class RadialStagePilot implements IApproachStagePilot<RadialStage> {
  @Override
  public eResult initStage(IPilot5Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public eResult flyStage(IPilot5Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public eResult disposeStage(IPilot5Behavior pilot, RadialStage stage) {
    return null;
  }

  @Override
  public boolean isFinishedStage(IPilot5Behavior pilot, RadialStage stage) {
    return false;
  }
}
