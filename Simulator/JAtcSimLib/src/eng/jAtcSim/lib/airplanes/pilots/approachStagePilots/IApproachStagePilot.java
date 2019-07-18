package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;

public interface IApproachStagePilot<T extends IApproachStage>  {
  enum eResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalLocation, runwayNotInSight, ok
  }

  eResult initStage(IPilotWriteSimple pilot, T stage);
  eResult flyStage(IPilotWriteSimple pilot, T stage);
  eResult disposeStage(IPilotWriteSimple pilot, T stage);
  boolean isFinishedStage(IPilotWriteSimple pilot, T stage);
}
