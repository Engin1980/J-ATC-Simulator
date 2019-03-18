package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public abstract class CheckApproachStage implements IApproachStage {

  public enum eCheckResult {
    speedTooHigh, speedTooLow, illegalHeading, altitudeTooHigh, altitudeTooLow, illegalDistance, runwayNotInSight, ok
  }

  @Override
  public void initStage(IPilot4Behavior pilot) {
    // intentionally blank
  }

  @Override
  public void flyStage(IPilot4Behavior pilot) {
    eCheckResult checkResult = check(pilot);
    if (checkResult != eCheckResult.ok) {
      throw new UnsupportedOperationException("Implement somehow");
    }
  }

  protected abstract eCheckResult check(IPilot4Behavior pilot);

  @Override
  public void disposeStage(IPilot4Behavior pilot) {
    // intentionally blank
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return true;
  }
}
