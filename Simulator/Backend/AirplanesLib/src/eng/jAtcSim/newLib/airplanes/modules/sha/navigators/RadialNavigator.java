package eng.jAtcSim.newLib.airplanes.modules.sha.navigators;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

public class RadialNavigator extends Navigator {
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

  public Coordinate getTargetCoordinate() {
    return this.coordinate;
  }

  @Override
  public NavigatorResult navigate(IAirplane plane) {
    double targetHeading = getHeadingToRadial(plane.getCoordinate(), this.coordinate, this.inboundRadial, this.mode);
    LeftRight turn = getBetterDirectionToTurn(plane.getSha().getHeading(), targetHeading);
    return new NavigatorResult((int) Math.round(targetHeading), turn);
  }
}
