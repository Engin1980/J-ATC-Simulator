package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public class FollowRadialStage implements IApproachStage {

  private double slope;
  private double radial;
  private Coordinates.eHeadingToRadialBehavior radialBehavior;
  private int lowerAltitude;
  private Coordinate lowerFix;
  private boolean isCloseToLowerFix;

  public FollowRadialStage(int upperAltitude, Coordinate upperFix,
                           int lowerAltitude, Coordinate lowerFix,
                           Coordinates.eHeadingToRadialBehavior radialBehavior) {
    this(lowerFix, lowerAltitude,
        Coordinates.getBearing(upperFix, lowerFix),
        calculateSlope(upperFix, upperAltitude, lowerFix, lowerAltitude),
        radialBehavior);
  }

  public FollowRadialStage(Coordinate targetFix, int targetAltitude,
                           double radial, double slope, Coordinates.eHeadingToRadialBehavior radialBehavior) {
    this.slope = slope;
    this.radial = radial;
    this.radialBehavior = radialBehavior;
    this.lowerAltitude = targetAltitude;
    this.lowerFix = targetFix;
    this.isCloseToLowerFix = false;
  }

  private static double calculateSlope(Coordinate upperFix, double upperAltitude, Coordinate lowerFix, double lowerAltitude){
    double distance = Coordinates.getDistanceInNM(upperFix, lowerFix);
    double altitude = upperAltitude - lowerAltitude;
    double ret = altitude / distance;
    return ret;
  }

  @Override
  public void initStage(IPilot4Behavior pilot) {
    // intentionally blank
  }

  @Override
  public void flyStage(IPilot4Behavior pilot) {
    flyHeading(pilot);
    flyAltitude(pilot);
  }

  private void flyAltitude(IPilot4Behavior pilot) {
    double alt = ApproachStages.getTargetAltitudeBySlope(pilot.getCoordinate(), pilot.getAltitude(),
        this.slope,this.lowerFix,this.lowerAltitude);
    pilot.setTargetAltitude(alt);
  }

  private void flyHeading(IPilot4Behavior pilot) {
    double headingToRadial = Coordinates.getHeadingToRadial(pilot.getCoordinate(),
        lowerFix,
        this.radial,
        this.radialBehavior);
    pilot.setTargetHeading(headingToRadial);
  }

  @Override
  public void disposeStage(IPilot4Behavior pilot) {
    // intentionally blank
  }

  @Override
  public boolean isFinishedStage(IPilot4Behavior pilot) {
    return this.isCloseToLowerFix;
  }
}
