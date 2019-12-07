package eng.jAtcSim.newLib.airplanes.approachStagePilots;

import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.world.approaches.stages.IApproachStage;

public interface IApproachStagePilot<T extends IApproachStage>  {
  enum eResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalLocation, runwayNotInSight, ok
  }

  eResult initStage(IAirplaneWriteSimple plane, T stage);
  eResult flyStage(IAirplaneWriteSimple plane, T stage);
  eResult disposeStage(IAirplaneWriteSimple plane, T stage);
  boolean isFinishedStage(IAirplaneWriteSimple plane, T stage);
}
