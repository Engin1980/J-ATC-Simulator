package eng.jAtcSim.lib.airplanes.navigators;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;

public class RadialNavigator implements INavigator2Coordinate {
  public enum AggresivityMode {
    gentle,
    standard,
    aggresive
  }

  public static double getHeadingToRadial(Coordinate current, Coordinate target, double radialToTarget, AggresivityMode mode) {
    double RADIAL_APPROACH_MULTIPLIER;
    double RADIAL_MAX_DIFF;
    switch (mode) {
      case gentle:
        RADIAL_APPROACH_MULTIPLIER = 4;
        RADIAL_MAX_DIFF = 15;
        break;
      case standard:
        RADIAL_APPROACH_MULTIPLIER = 4;
        RADIAL_MAX_DIFF = 30;
        break;
      case aggresive:
        RADIAL_APPROACH_MULTIPLIER = 7;
        RADIAL_MAX_DIFF = 45;
        break;
      default:
        throw new EEnumValueUnsupportedException(mode);
    }

    double heading = Coordinates.getBearing(current, target);
    double diff = Headings.subtract(heading, radialToTarget);
    diff *= RADIAL_APPROACH_MULTIPLIER;
    if (Math.abs(diff) > RADIAL_MAX_DIFF) {
      diff = Math.signum(diff) * RADIAL_MAX_DIFF;
    }

    double ret = radialToTarget + diff;
    ret = Headings.to(ret);
    return ret;
  }

  private final Coordinate coordinate;
  private final int inboundRadial;
  private final AggresivityMode mode;

  public RadialNavigator(Coordinate coordinate, int inboundRadial, AggresivityMode mode) {
    this.coordinate = coordinate;
    this.inboundRadial = inboundRadial;
    this.mode = mode;
  }

  @Override
  public void navigate(ShaModule sha, Coordinate planeCoordinate) {
    double targetHeading = getHeadingToRadial(planeCoordinate, this.coordinate, this.inboundRadial, this.mode);
    sha.setNavigator(
        new HeadingNavigator(targetHeading));
  }

  @Override
  public Coordinate getTargetCoordinate() {
    return this.coordinate;
  }
}
