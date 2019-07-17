package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;

public interface IApproachStagePilot<T extends IApproachStage>  {
  enum eResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalDistance, runwayNotInSight, ok
  }

  eResult initStage(IPilot5Behavior pilot, T stage);
  eResult flyStage(IPilot5Behavior pilot, T stage);
  eResult disposeStage(IPilot5Behavior pilot, T stage);
  boolean isFinishedStage(IPilot5Behavior pilot, T stage);
}
