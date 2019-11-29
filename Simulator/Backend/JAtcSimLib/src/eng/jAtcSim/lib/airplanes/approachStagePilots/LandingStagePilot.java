package eng.jAtcSim.lib.airplanes.approachStagePilots;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.world.approaches.stages.LandingStage;

public class LandingStagePilot implements IApproachStagePilot<LandingStage> {
  @Override
  public eResult disposeStage(IAirplaneWriteSimple plane, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public eResult flyStage(IAirplaneWriteSimple plane, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public eResult initStage(IAirplaneWriteSimple plane, LandingStage stage) {
    throw new ToDoException();
  }

  @Override
  public boolean isFinishedStage(IAirplaneWriteSimple plane, LandingStage stage) {
    throw new ToDoException();
  }
}
