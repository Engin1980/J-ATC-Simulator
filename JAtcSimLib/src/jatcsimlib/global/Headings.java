/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

import jatcsimlib.speaking.fromAtc.commands.ChangeHeadingCommand;

/**
 * @author Marek
 */
public class Headings {

  private Headings() {
  }

  public static Object format(int heading) {
    return String.format("%03d", heading);
  }

  public static int getDifference(int a, int b, boolean useShortestArc) {
    int ret;
    if (useShortestArc)
      ret = getDifferenceShortestArc(a, b);
    else
      ret = getDifferenceAnyArc(a, b);
    return ret;
  }

  public static int subtract(int a, int b) {
    int ret = a - b;
    ret = to(ret);
    return ret;
  }

  private static int getDifferenceShortestArc(int a, int b) {
    int ret;

    // ensure a <= b
    if (a > b) {
      int c = a;
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

  private static int getDifferenceAnyArc(int a, int b) {
    int ret = a - b;
    ret = to(ret);
    return ret;
  }

  public static ChangeHeadingCommand.eDirection getBetterDirectionToTurn(int current, int target) {
    target = target - current;
    target = to(target);
    if (target > 180)
      return ChangeHeadingCommand.eDirection.left;
    else
      return ChangeHeadingCommand.eDirection.right;
  }

  @Deprecated
  public static int turn(int current, int amount, boolean toLeft, int targetValue) {
    //TODO this should be responsibility of airplane, not of headings class.
    // should be rebuilt in different way

    int ret;
    boolean targeted;
    if (toLeft) {
      ret = Headings.add(current, -amount);
      targeted = Headings.isBetween(ret, targetValue, current);
    } else {
      ret = Headings.add(current, amount);
      targeted = Headings.isBetween(current, targetValue, ret);
    }

    if (targeted)
      ret = targetValue;
    return ret;
  }

  public static boolean isBetween(int leftBorder, int value, int rightBorder) {
    value -= leftBorder;
    rightBorder -= leftBorder;

    leftBorder = 0;
    value = to(value);
    rightBorder = to(rightBorder);

    return leftBorder <= value && value <= rightBorder;
  }

  public static int to(int value) {
    int ret = value;
    while (ret < 0)
      ret += 360;
    while (ret > 360)
      ret -= 360; // probably faster than modulo
    return ret;
  }

  public static int add(int current, int amount) {
    int ret = current + amount;
    ret = to(ret);
    return ret;
  }
}
