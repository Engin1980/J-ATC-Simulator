package eng.jAtcSim.newLib.shared;

import eng.eSystem.Tuple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.geometry2D.Line;

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

  private final static double ALIGNED_TO_RADIAL_LINE_DISTANCE = 0.001;
  private final static double CLOSE_TO_RADIAL_LINE_DISTANCE = .05;
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
    eRadialLocation radialLocation;
    Line radialLine = getRadialLine(fix, radial);
    double turnRadius = calculateTurnRadius(speedInKt);

    double distanceToRadialLine = evaluateDistanceToRadialLine(currentPosition, radialLine);
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
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, (int) Math.min(10, maxHeadingDifference));
        break;
      case capturing:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, (int) Math.min(15, maxHeadingDifference));
        break;
      case close:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, (int) Math.min(30, maxHeadingDifference));
        break;
      case far:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, (int) Math.min(90, maxHeadingDifference));
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

  private static double getHeadingInAlignment(Coordinate current, double radial, Line radialLine, int maxDifference) {
    double ret;
    double distance = evaluateDistanceToRadialLine(current, radialLine);
    Line.eSide side = radialLine.getRelativeLocation(current.getLatitude().get(), current.getLongitude().get());
    double headingDifference = distance * CAPTURE_AGGRESIVITY;
    headingDifference = Math.min(headingDifference, maxDifference);
    if (side == Line.eSide.left)
      ret = radial - headingDifference;
    else
      ret = radial + headingDifference;
    return ret;
  }

  private static double evaluateDistanceToRadialLine(Coordinate currentPosition, Line radialLine) {
    double ret = radialLine.getDistance(
            currentPosition.getLatitude().get(), currentPosition.getLongitude().get());
    return ret;
  }

  private static Line getRadialLine(Coordinate a, double radial) {
    Coordinate b = Coordinates.getCoordinate(a, radial, 10); // number is random
    Line ret = new Line(a.getLatitude().get(), a.getLongitude().get(), b.getLatitude().get(), b.getLongitude().get());
    return ret;
  }
}
