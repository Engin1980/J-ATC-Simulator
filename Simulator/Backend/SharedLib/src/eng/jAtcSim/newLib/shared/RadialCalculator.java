package eng.jAtcSim.newLib.shared;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.geometry2D.Line;
import eng.jAtcSim.newLib.shared.enums.LeftRight;

public class RadialCalculator {
  public enum eToPointLocation {
    inFrontLeft(0),
    inFrontRight(1),
    behindLeft(2),
    behindRight(3);

    int value;

    eToPointLocation(int value) {
      this.value = value;
    }

    boolean isBehind() {
      return this == behindLeft || this == behindRight;
    }

    boolean isLeft() {
      return this == behindLeft || this == inFrontLeft;
    }
  }

  private enum eRadialLocation {
    aligned,
    capturing,
    close,
    far
  }

  private final static double ALIGNED_TO_RADIAL_LINE_DISTANCE = 0.25;
  private final static double CLOSE_TO_RADIAL_LINE_DISTANCE = 1.0;
  private final static double CAPTURE_AGGRESIVITY = 10000;
  private static final double CUSTOM_EXTENSION_TO_SUPPRESS_WIND_AND_OTHER_INFLUENCES = 0.05;

  public static double getHeadingToFollowRadial(Coordinate currentPosition, Coordinate fix, double radial,
                                                double speedInKt) {
    double ret = getHeadingToFollowRadial(currentPosition, fix, radial, speedInKt, 360);
    return ret;
  }

  public static double getHeadingToFollowRadial(Coordinate currentPosition, Coordinate fix, double radial,
                                                double speedInKt, double maxHeadingDifference) {
    double ret;

    LeftRight sideToRadialLine = getSideFromRadial(currentPosition, fix, radial);
    double distanceToRadialLine = evaluateDistanceToRadialLine2(currentPosition, sideToRadialLine, fix, radial);
    double turnRadius = calculateTurnRadius(speedInKt);

    eRadialLocation radialLocation;
    if (distanceToRadialLine < ALIGNED_TO_RADIAL_LINE_DISTANCE)
      radialLocation = eRadialLocation.aligned;
    else if (distanceToRadialLine < CLOSE_TO_RADIAL_LINE_DISTANCE)
      radialLocation = eRadialLocation.capturing;
    else if (distanceToRadialLine < (turnRadius + CUSTOM_EXTENSION_TO_SUPPRESS_WIND_AND_OTHER_INFLUENCES))
      radialLocation = eRadialLocation.close;
    else
      radialLocation = eRadialLocation.far;

    switch (radialLocation) {
      case aligned:
        ret = getHeadingInAlignment(radial, distanceToRadialLine, sideToRadialLine, (int) Math.min(10, maxHeadingDifference));
        break;
      case capturing:
        ret = getHeadingInAlignment(radial, distanceToRadialLine, sideToRadialLine, (int) Math.min(15, maxHeadingDifference));
        break;
      case close:
        ret = getHeadingInAlignment(radial, distanceToRadialLine, sideToRadialLine, (int) Math.min(30, maxHeadingDifference));
        break;
      case far:
        ret = getHeadingInAlignment(radial, distanceToRadialLine, sideToRadialLine, (int) Math.min(90, maxHeadingDifference));
        break;
      default:
        throw new EEnumValueUnsupportedException(radialLocation);
    }

    ret = Headings.to(ret);
    return ret;
  }

  private static double calculateTurnRadius(double speedInKt) {
    double distanceInTwoMinutes = speedInKt / 30d;
    double radius = distanceInTwoMinutes / 2 / Math.PI;
    return radius;
  }

  private static double getHeadingInAlignment(double radial,
                                              double distanceToRadial, LeftRight sideToRadial,
                                              int maxDifference) {
    double ret;
    double distance = distanceToRadial;
    LeftRight side = sideToRadial;
    double headingDifference = distance * CAPTURE_AGGRESIVITY;
    headingDifference = Math.min(headingDifference, maxDifference);
    if (side == LeftRight.left)
      ret = radial - headingDifference;
    else
      ret = radial + headingDifference;
    return ret;
  }

  private static LeftRight getSideFromRadial(Coordinate position, Coordinate fix, double fixRadial) {
    LeftRight ret = Headings.isBetween(
            fixRadial,
            Coordinates.getBearing(fix, position),
            Headings.add(fixRadial, 180))
            ? LeftRight.right : LeftRight.left;
    return ret;
  }

  private static double evaluateDistanceToRadialLine2(Coordinate position, LeftRight positionSide, Coordinate fix, double fixRadial) {
    double positionRadial = positionSide == LeftRight.left ?
            Headings.add(fixRadial, +90) :
            Headings.add(fixRadial, -90);
    Coordinate intersection = Coordinates.getIntersection(position, positionRadial, fix, Headings.getOpposite(fixRadial));
    double dist = Coordinates.getDistanceInNM(intersection, position);
    return dist;
  }

  private static Line getRadialLine(Coordinate a, double radial) {
    Coordinate b = Coordinates.getCoordinate(a, radial, 10); // number is random
    Line ret = new Line(a.getLatitude().get(), a.getLongitude().get(), b.getLatitude().get(), b.getLongitude().get());
    return ret;
  }
}
