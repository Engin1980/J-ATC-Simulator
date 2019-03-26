/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.eSystem.Tuple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geometry2D.Line;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

/**
 * @author Marek
 */
public class Headings {

  public static Object format(int heading) {
    return String.format("%03d", heading);
  }

  public static double getDifference(double fromHeading, double toHeading, boolean useShortestArc) {
    double ret;
    if (useShortestArc)
      ret = getDifferenceShortestArc(fromHeading, toHeading);
    else
      ret = getDifferenceAnyArc(fromHeading, toHeading);
    return ret;
  }

  public static double subtract(double a, double b) {
    if (Math.abs(a - b) > 180) {
      if (a < b)
        a += 360;
      else
        b += 360;
    }
    double ret = a - b;
    return ret;
  }

  public static ChangeHeadingCommand.eDirection getBetterDirectionToTurn(double current, double target) {
    target = target - current;
    target = to(target);
    if (target > 180)
      return ChangeHeadingCommand.eDirection.left;
    else
      return ChangeHeadingCommand.eDirection.right;
  }

  public static boolean isBetween(double leftBorder, double value, double rightBorder) {
    value -= leftBorder;
    rightBorder -= leftBorder;

    leftBorder = 0;
    value = to(value);
    rightBorder = to(rightBorder);

    return leftBorder <= value && value <= rightBorder;
  }

  public static double to(double value) {
    double ret = value;
    while (ret < 0)
      ret += 360;
    while (ret >= 360)
      ret -= 360; // probably faster than modulo
    return ret;
  }

  public static double add(double current, double amount) {
    double ret = current + amount;
    ret = to(ret);
    return ret;
  }

  public static int add(int current, int amount) {
    double ret = current + amount;
    ret = to(ret);
    return (int) ret;
  }

  public static double getOpposite(double course) {
    double ret = course - 180;
    ret = Headings.to(ret);
    return ret;
  }

  public static double getHeadingToFollowRadial(Coordinate currentPosition, Coordinate radialSourcePosition, double radial,
                                                double maxHeadingDifference, double speedInKt) {
    double ret = RadialCalculator.getHeadingToFollowRadial(currentPosition, radialSourcePosition, radial,
        maxHeadingDifference, speedInKt);
    return ret;
  }

  private static double getDifferenceShortestArc(double a, double b) {
    double ret;

    // ensure a <= b
    if (a > b) {
      double c = a;
      a = b;
      b = c;
    }

    ret = b - a;
    if (ret > 180) {
      // if is over 180, the other side might be closer
      ret = (a + 360) - b;
    }

    return ret;
  }

  private static double getDifferenceAnyArc(double fromHeading, double toHeading) {
    double ret = fromHeading - toHeading;
    ret = to(ret);
    return ret;
  }

  private Headings() {
  }
}

class RadialCalculator {
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

    eToPointLocation positionToFix = evaluateLocationToPoint(currentPosition, fix, radial);
    if (positionToFix.isBehind())
      radialLocation = eRadialLocation.behind;
    else {
      double distanceToRadialLine = evaluateDistanceToRadialLine(currentPosition, fix, radial);
      if (distanceToRadialLine < ALIGNED_TO_RADIAL_LINE_DISTANCE)
        radialLocation = eRadialLocation.aligned;
      else if (distanceToRadialLine < CLOSE_TO_RADIAL_LINE_DISTANCE)
        radialLocation = eRadialLocation.close;
      else {
        Tuple<Coordinate, Double> tcc = evaluateTurnCircleCenter(fix, radial, positionToFix.isLeft(), speedInKt);
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

      tady dopsat kejsy jak se to letí

//      case unset:
//        throw new EApplicationException("Unable to evaluate plane location according to the radial data.");
//        break;
    }

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

  private static double evaluateDistanceToRadialLine(Coordinate currentPosition, Coordinate fix, double radial) {
    Line line = getRadialLine(fix, radial);
    double ret = line.getDistance(
        currentPosition.getLatitude().get(), currentPosition.getLongitude().get());
    return ret;
  }


  private static Tuple<Coordinate, Double> evaluateTurnCircleCenter(Coordinate fix, double radial, boolean isOnLeft, double speedInKt) {
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
