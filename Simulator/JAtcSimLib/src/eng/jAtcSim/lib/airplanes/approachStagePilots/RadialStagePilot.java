package eng.jAtcSim.lib.airplanes.approachStagePilots;

import eng.jAtcSim.lib.airplanes.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.navigators.RadialNavigator;
import eng.jAtcSim.lib.world.newApproaches.stages.RadialStage;

public class RadialStagePilot implements IApproachStagePilot<RadialStage> {
  @Override
  public eResult initStage(IPilotWriteSimple pilot, RadialStage stage) {
    pilot.setNavigator(
        new RadialNavigator(stage.getFix(), stage.getInboundRadial(), RadialNavigator.AggresivityMode.standard));
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilotWriteSimple pilot, RadialStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilotWriteSimple pilot, RadialStage stage) {
    pilot.setNavigator(
        new HeadingNavigator(pilot.getPlane().getSha().getTargetHeading()));
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilotWriteSimple pilot, RadialStage stage) {
    return stage.getExitCondition().isTrue(pilot);
  }
}
