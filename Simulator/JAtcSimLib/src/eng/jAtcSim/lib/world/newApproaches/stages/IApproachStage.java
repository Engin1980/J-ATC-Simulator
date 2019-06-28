package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public interface IApproachStage {

  enum eResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalDistance, runwayNotInSight, ok
  }

  eResult initStage(IPilot4Behavior pilot);
  eResult flyStage(IPilot4Behavior pilot);
  eResult disposeStage(IPilot4Behavior pilot);
  boolean isFinishedStage(IPilot4Behavior pilot);
}
