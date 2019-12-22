package eng.jAtcSim.newLib.area.airplanes.approachStagePilots;

import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.airplanes.navigators.RadialNavigator;
import eng.jAtcSim.newLib.world.approaches.stages.RadialStage;

public class RadialStagePilot implements IApproachStagePilot<RadialStage> {
  @Override
  public eResult initStage(IAirplaneWriteSimple plane, RadialStage stage) {
    plane.setNavigator(
        new RadialNavigator(stage.getCoordinate(), stage.getInboundRadial(), RadialNavigator.AggresivityMode.standard));
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IAirplaneWriteSimple plane, RadialStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IAirplaneWriteSimple plane, RadialStage stage) {
    plane.setNavigator(
        new HeadingNavigator(plane.getSha().getTargetHeading()));
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IAirplaneWriteSimple plane, RadialStage stage) {
    return stage.getExitCondition().isTrue(plane);
  }
}
