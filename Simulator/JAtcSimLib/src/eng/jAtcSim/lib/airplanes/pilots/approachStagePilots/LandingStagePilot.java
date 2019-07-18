package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.world.newApproaches.stages.LandingStage;

public class LandingStagePilot implements IApproachStagePilot<LandingStage> {
  @Override
  public eResult disposeStage(IPilotWriteSimple pilot, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public eResult flyStage(IPilotWriteSimple pilot, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public eResult initStage(IPilotWriteSimple pilot, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public boolean isFinishedStage(IPilotWriteSimple pilot, LandingStage stage) {
    throw new ToDoException();
  }
}
