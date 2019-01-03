/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

/**
 * @author Marek
 */
public class Headings {

  private Headings() {
  }

  public static Object format(int heading) {
    return String.format("%03d", heading);
  }

  public static double getDifference(double a, double b, boolean useShortestArc) {
    double ret;
    if (useShortestArc)
      ret = getDifferenceShortestArc(a, b);
    else
      ret = getDifferenceAnyArc(a, b);
    return ret;
  }

  public static double subtract(double a, double b) {
    if (Math.abs(a-b) > 180){
      if (a < b)
        a += 360;
      else
        b += 360;
    }
    double ret = a - b;
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

  private static double getDifferenceAnyArc(double a, double b) {
    double ret = a - b;
    ret = to(ret);
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

  public static double getOpposite(double course) {
    double ret = course - 180;
    ret = Headings.to(ret);
    return ret;
  }
}
