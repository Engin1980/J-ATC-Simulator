package eng.jAtcSim.lib.airplanes.pilots.approachStagePilots;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.world.newApproaches.stages.FlyToPointStage;

public class FlyToPointStagePilot implements IApproachStagePilot<FlyToPointStage> {
  private static final double OVER_THE_POINT_DISTANCE_NM = 1; // in NM

  @Override
  public eResult initStage(IPilot4Behavior pilot, FlyToPointStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IPilot4Behavior pilot, FlyToPointStage stage) {
    double heading = Coordinates.getBearing(pilot.getCoordinate(), stage.getCoordinate());
    pilot.setTargetHeading(heading);
    return eResult.ok;
  }

  @Override
  public eResult disposeStage(IPilot4Behavior pilot, FlyToPointStage stage) {
    return eResult.ok;
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot, FlyToPointStage stage) {
    double distance = Coordinates.getDistanceInNM(pilot.getCoordinate(), stage.getCoordinate());
    boolean ret;
    ret = distance < OVER_THE_POINT_DISTANCE_NM;
    return ret;
  }
}
