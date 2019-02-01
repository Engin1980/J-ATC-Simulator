package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;

public class ApproachStages {

  private ApproachStages(){}

  public static double getTargetAltitudeBySlope(Coordinate currentFix, double currentAltitude, double slope, Coordinate targetFix, double targetAltitude) {
    double distance = Coordinates.getDistanceInNM(currentFix, targetFix);
    double ret = distance * slope + targetAltitude;
    ret = Math.min(ret, currentAltitude);
    return ret;
  }

}
