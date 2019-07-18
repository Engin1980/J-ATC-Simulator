package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.ToCoordinateNavigator;
import eng.jAtcSim.lib.world.newApproaches.stages.FlyToPointStage;

public class FlyToPointStagePilot implements IApproachStagePilot<FlyToPointStage> {
  private static final double OVER_THE_POINT_DISTANCE_NM = 1; // in NM

  @Override
  public eResult initStage(IPilotWriteSimple pilot, FlyToPointStage stage) {
    pilot.setNavigator(
        new ToCoordinateNavigator(stage.getCoordinate())
    );

    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilotWriteSimple pilot, FlyToPointStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilotWriteSimple pilot, FlyToPointStage stage) {
    pilot.setNavigator(
        new HeadingNavigator(pilot.getPlane().getSha().getTargetHeading())
    );
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilotWriteSimple pilot, FlyToPointStage stage) {
    double distance = Coordinates.getDistanceInNM(pilot.getPlane().getCoordinate(), stage.getCoordinate());
    boolean ret;
    ret = distance < OVER_THE_POINT_DISTANCE_NM;
    return ret;
  }
}
