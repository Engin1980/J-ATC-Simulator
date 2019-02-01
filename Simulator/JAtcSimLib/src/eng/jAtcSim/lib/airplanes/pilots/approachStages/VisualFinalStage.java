package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.world.RunwayThreshold;

/**
 * Represents visual approach aimed to the runway threshold.
 * <p>
 * The point is that the airplane selects the specified point PX:
 * <ul>
 * <li>If the airplane is less than 2nm to the threshold, that point is in the middle distance
 * between the plane an the runway</li>
 * <li>If the airplane is more than 2nm to the threshold, that point is randomly between 2 and 5 nm
 * from the runway threshold</li>
 * </ul>
 * a) if the aip
 */
public class VisualFinalStage implements IApproachStage {
  private static final double MINIMAL_AIM_POINT_DISTANCE = .4;
  private static final double MINIMAL_NORMAL_AIM_POINT_DISTANCE = 2;
  private static final double MAXIMAL_NORMAL_AIM_POINT_DISTANCE = 5;
  private static final double AIP_POINT_DELETE_DISTANCE = 1;
  private static final double DEFAULT_SLOPE = 1;
  private RunwayThreshold threshold;
  private Coordinate aimPoint;

  public VisualFinalStage(RunwayThreshold threshold) {
    this.threshold = threshold;
  }

  @Override
  public void initStage(IPilot4Behavior pilot) {
    double distanceFromThreshold = Coordinates.getDistanceInNM(threshold.getCoordinate(), pilot.getCoordinate());
    double aimPointDistance;
    if (distanceFromThreshold > MINIMAL_NORMAL_AIM_POINT_DISTANCE) {
      aimPointDistance = Acc.rnd().nextDouble(MINIMAL_NORMAL_AIM_POINT_DISTANCE, MAXIMAL_NORMAL_AIM_POINT_DISTANCE);
    } else {
      aimPointDistance = MINIMAL_NORMAL_AIM_POINT_DISTANCE / 2;
      if (aimPointDistance < MINIMAL_AIM_POINT_DISTANCE)
        aimPointDistance = MINIMAL_AIM_POINT_DISTANCE;
    }

    this.aimPoint = Coordinates.getCoordinate(this.threshold.getCoordinate(),
        threshold.getOtherThreshold().getCourse(),
        aimPointDistance);
  }

  @Override
  public void flyStage(IPilot4Behavior pilot) {
    if (aimPoint != null) {
      doFlyHeading(aimPoint, pilot);
      deleteAimPointIfClose(pilot);
    }
    else
      doFlyHeading(this.threshold.getCoordinate(), pilot);

    doFlyAltitude(pilot);
  }

  private void doFlyAltitude(IPilot4Behavior pilot) {
    double alt = ApproachStages.getTargetAltitudeBySlope(pilot.getCoordinate(), pilot.getAltitude(),
        DEFAULT_SLOPE, this.threshold.getCoordinate(), this.threshold.getParent().getParent().getAltitude());
    pilot.setTargetAltitude(alt);
  }

  private void deleteAimPointIfClose(IPilot4Behavior pilot) {
    double dst = Coordinates.getDistanceInNM(pilot.getCoordinate(), aimPoint);
    if (dst < AIP_POINT_DELETE_DISTANCE)
      this.aimPoint = null;
  }

  private void doFlyHeading(Coordinate point, IPilot4Behavior pilot) {
    double hdg = Coordinates.getBearing(pilot.getCoordinate(), point);
    pilot.setTargetHeading(hdg);
  }

  @Override
  public void disposeStage(IPilot4Behavior pilot) {
// intentionally blank
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return false;
  }
}
