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
  private int upperAltitude;
  private Coordinate upperFix;
  private boolean isCloseToLowerFix;

  public FollowRadialStage(int upperAltitude, Coordinate upperFix,
                           int lowerAltitude, Coordinate lowerFix,
                           Coordinates.eHeadingToRadialBehavior radialBehavior) {
    this.slope = calculateSlope(upperFix, upperAltitude, lowerFix, lowerAltitude);
    this.radial = Coordinates.getBearing(upperFix, lowerFix);
    this.radialBehavior = radialBehavior;
    this.lowerAltitude = lowerAltitude;
    this.lowerFix = lowerFix;
    this.upperAltitude = upperAltitude;
    this.upperFix = upperFix;
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

  public Coordinate getLowerCoordinate() {
    return this.lowerFix;
  }

  public Coordinate getUpperCoordinate(){
    return this.upperFix;
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
