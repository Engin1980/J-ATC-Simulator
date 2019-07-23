package eng.jAtcSim.lib.airplanes.approachStagePilots;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.navigators.ToCoordinateNavigator;
import eng.jAtcSim.lib.world.approaches.stages.FlyToPointStage;

public class FlyToPointStagePilot implements IApproachStagePilot<FlyToPointStage> {
  private static final double OVER_THE_POINT_DISTANCE_NM = 1; // in NM

  @Override
  public eResult initStage(IAirplaneWriteSimple plane, FlyToPointStage stage) {
    plane.setNavigator(
        new ToCoordinateNavigator(stage.getCoordinate())
    );

    return eResult.ok;
  }

  @Override
  public eResult flyStage(IAirplaneWriteSimple plane, FlyToPointStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IAirplaneWriteSimple plane, FlyToPointStage stage) {
    plane.setNavigator(
        new HeadingNavigator(plane.getSha().getTargetHeading())
    );
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IAirplaneWriteSimple plane, FlyToPointStage stage) {
    double distance = Coordinates.getDistanceInNM(plane.getCoordinate(), stage.getCoordinate());
    boolean ret;
    ret = distance < OVER_THE_POINT_DISTANCE_NM;
    return ret;
  }
}
