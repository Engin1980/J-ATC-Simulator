package eng.jAtcSim.newLib.shared;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.geometry2D.Line;

import static eng.eSystem.utilites.FunctionShortcuts.*;

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

  public enum eRadialLocation {
    unset,
    behind,
    aligned,
    close,
    unturnable,
    atTurnBelt,
    freeArea
  }

  private final static double ALIGNED_TO_RADIAL_LINE_DISTANCE = .7;
  private final static double CLOSE_TO_RADIAL_LINE_DISTANCE = 2;
  private final static double TCC_CAPTURED_RADIUS_WIDTH = 2;

  public static double getHeadingToFollowRadial(Coordinate currentPosition, Coordinate fix, double radial,
                                                double maxHeadingDifference, double speedInKt) {

    double ret;
    eRadialLocation radialLocation;
    Tuple<Coordinate, Double> tcc = null; // coordinate and turn radius
    Line radialLine = null;

    eToPointLocation positionToFix = evaluateLocationToPoint(currentPosition, fix, radial);
    if (positionToFix.isBehind())
      radialLocation = eRadialLocation.behind;
    else {
      radialLine = getRadialLine(fix, radial);
      double distanceToRadialLine = evaluateDistanceToRadialLine(currentPosition, radialLine);
      if (distanceToRadialLine < ALIGNED_TO_RADIAL_LINE_DISTANCE)
        radialLocation = eRadialLocation.aligned;
      else if (distanceToRadialLine < CLOSE_TO_RADIAL_LINE_DISTANCE)
        radialLocation = eRadialLocation.close;
      else {
        tcc = evaluateTurnCircleCenter(fix, radial, positionToFix.isLeft(), speedInKt);
        eToPointLocation positionToTcc = evaluateLocationToPoint(currentPosition, tcc.getA(), radial);
        if (positionToTcc.isBehind())
          radialLocation = eRadialLocation.unturnable;
        else {
          double distanceToTcc = Coordinates.getDistanceInNM(tcc.getA(), currentPosition);
          if (distanceToTcc < tcc.getB())
            radialLocation = eRadialLocation.unturnable;
          else if (distanceToTcc < (tcc.getB() + TCC_CAPTURED_RADIUS_WIDTH))
            radialLocation = eRadialLocation.atTurnBelt;
          else
            radialLocation = eRadialLocation.freeArea;
        }

      }
    }

    switch (radialLocation) {
      case unturnable:
      case behind:
        ret = Double.NaN;
        break;
      case close:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, 10);
        break;
      case aligned:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, 30);
        break;
      case freeArea:
        assert radialLine != null;
        ret = getHeadingInAlignment(currentPosition, radial, radialLine, 45);
        break;
      case atTurnBelt:
        assert tcc != null;
        ret = getHeadingAtTurnBelt(currentPosition, tcc.getA());
        break;
      default: // including "unset"
//      case unset:
        throw new EApplicationException("Unable to evaluate plane location according to the radial data.");
    }

    return ret;
  }

  private static double getHeadingAtTurnBelt(Coordinate current, Coordinate turnCenter) {
    double ret = Coordinates.getBearing(turnCenter, current);
    return ret;
  }

  private static double getHeadingInAlignment(Coordinate current, double radial, Line radialLine, int maxDifference) {
    double ret;
    double distance = evaluateDistanceToRadialLine(current, radialLine);
    Line.eSide side = radialLine.getRelativeLocation(current.getLatitude().get(), current.getLongitude().get());
    double headingDifference = distance / ALIGNED_TO_RADIAL_LINE_DISTANCE * 15;
    headingDifference = Math.max(headingDifference, maxDifference);
    if (side == Line.eSide.left)
      ret = radial - headingDifference;
    else
      ret = radial + headingDifference;
    return ret;
  }

  private static eToPointLocation evaluateLocationToPoint(Coordinate point, Coordinate center, double radial) {
    eToPointLocation ret;
    double pointRadial = Coordinates.getBearing(center, point);
    double diff = Headings.getDifference(radial, pointRadial, false);
    if (diff <= 90)
      ret = eToPointLocation.inFrontRight;
    else if (diff <= 180)
      ret = eToPointLocation.behindRight;
    else if (diff <= 270)
      ret = eToPointLocation.behindLeft;
    else
      ret = eToPointLocation.inFrontLeft;
    return ret;
  }

  private static double evaluateDistanceToRadialLine(Coordinate currentPosition, Line radialLine) {
    double ret = radialLine.getDistance(
        currentPosition.getLatitude().get(), currentPosition.getLongitude().get());
    return ret;
  }


  private static Tuple<Coordinate, Double> evaluateTurnCircleCenter(
      Coordinate fix, double radial, boolean isOnLeft, double speedInKt) {
    double flownDistance = speedInKt / 30;
    double radius = flownDistance / 2 / Math.PI;
    double directionFromFix = isOnLeft ? Headings.add(radial, 90) : Headings.add(radial, -90);
    Coordinate coordinate = Coordinates.getCoordinate(fix, directionFromFix, radius);
    return new Tuple<>(coordinate, radius);
  }

  private static Line getRadialLine(Coordinate a, double radial) {
    Coordinate b = Coordinates.getCoordinate(a, radial, 10); // number is random
    Line ret = new Line(a.getLatitude().get(), a.getLongitude().get(), b.getLatitude().get(), b.getLongitude().get());
    return ret;
  }
}
