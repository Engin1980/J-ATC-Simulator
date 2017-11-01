/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

import jatcsimlib.speaking.commands.specific.ChangeHeadingCommand;

/**
 *
 * @author Marek
 */
public class Headings {

  public static Object format(int heading) {
    return String.format("%03d", heading);
  }

  public static int diff(int a, int b) {
    int ret = a - b;
    ret = to(ret);
    return ret;
  }
  private Headings(){}
  
  public static ChangeHeadingCommand.eDirection getBetterDirectionToTurn(int current, int target){
    target = target - current;
    target = to(target);
    if (target > 180)
      return ChangeHeadingCommand.eDirection.left;
    else
      return ChangeHeadingCommand.eDirection.right;
  }
  
  public static int turn (int current, int amount, boolean toLeft, int targetValue){
    
    int ret;
    boolean targeted;
    if (toLeft){
      ret = Headings.add(current,-amount);
      targeted = Headings.isBetween(ret, targetValue, current);
    } else {
      ret = Headings.add(current,amount);
      targeted = Headings.isBetween(current, targetValue, ret);
    }
    
    if (targeted)
      ret = targetValue;
    return ret;
  }
  
  public static boolean isBetween (int leftBorder, int value, int rightBorder){
    value -= leftBorder;
    rightBorder -= leftBorder;
    
    leftBorder = 0;
    value = to(value);
    rightBorder = to(rightBorder);
    
    return leftBorder <= value && value <= rightBorder;
  }
  
  public static int to (int value){
    int ret = value;
    while (ret < 0)
      ret += 360;
    while (ret > 360)
      ret -= 360; // probably faster than modulo
    return ret;
  }
  
  public static int add (int current, int amount){
    int ret = current + amount;
    ret = to(ret);
    return ret;
  }
}
