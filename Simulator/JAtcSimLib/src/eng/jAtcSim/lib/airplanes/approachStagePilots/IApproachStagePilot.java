package eng.jAtcSim.lib.airplanes.approachStagePilots;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.world.approaches.stages.IApproachStage;

public interface IApproachStagePilot<T extends IApproachStage>  {
  enum eResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalLocation, runwayNotInSight, ok
  }

  eResult initStage(IAirplaneWriteSimple plane, T stage);
  eResult flyStage(IAirplaneWriteSimple plane, T stage);
  eResult disposeStage(IAirplaneWriteSimple plane, T stage);
  boolean isFinishedStage(IAirplaneWriteSimple plane, T stage);
}
