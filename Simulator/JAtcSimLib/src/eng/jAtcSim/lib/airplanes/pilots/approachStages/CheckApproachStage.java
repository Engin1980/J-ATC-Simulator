package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public abstract class CheckApproachStage implements IApproachStage {

  @Override
  public eResult initStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot) {
    eResult checkResult = check(pilot);
    return checkResult;
  }

  protected abstract eResult check(IPilot4Behavior pilot);

  @Override
  public eResult disposeStage(IPilot4Behavior pilot) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return true;
  }
}
