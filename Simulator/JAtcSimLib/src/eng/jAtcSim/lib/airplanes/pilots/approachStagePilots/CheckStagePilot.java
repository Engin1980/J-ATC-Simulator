package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.world.newApproaches.stages.CheckStage;
import eng.jAtcSim.lib.world.newApproaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.lib.world.newApproaches.stages.checks.CheckPlaneLocationStage;
import eng.jAtcSim.lib.world.newApproaches.stages.checks.CheckPlaneStateStage;

public class CheckStagePilot implements IApproachStagePilot<CheckStage> {

  @Override
  public eResult initStage(IPilot4Behavior pilot, CheckStage stage) {
    eResult ret;
    if (stage instanceof CheckAirportVisibilityStage)
      ret = evaluateCheckAirportVisibility(pilot, (CheckAirportVisibilityStage) stage);
    else if (stage instanceof CheckPlaneLocationStage)
      ret = evaluateCheckPlaneLocationStage(pilot, (CheckPlaneLocationStage) stage);
    else if (stage instanceof CheckPlaneStateStage)
      ret = evaluateCheckPlaneStateStage(pilot, (CheckPlaneStateStage) stage);
    else
      throw new EApplicationException("CheckStagePilot does not support stage of type '" + stage.getClass().getSimpleName() + "'.");

    return ret;
  }

  private eResult evaluateCheckPlaneStateStage(IPilot4Behavior pilot, CheckPlaneStateStage stage) {
    throw new UnsupportedOperationException("TODO");
  }

  private eResult evaluateCheckPlaneLocationStage(IPilot4Behavior pilot, CheckPlaneLocationStage stage) {
    throw new UnsupportedOperationException("TODO");
  }

  private eResult evaluateCheckAirportVisibility(IPilot4Behavior pilot, CheckAirportVisibilityStage stage) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot, CheckStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilot4Behavior pilot, CheckStage stage) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot, CheckStage stage) {
    return true;
  }
}
