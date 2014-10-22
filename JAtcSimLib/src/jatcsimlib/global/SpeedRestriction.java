/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.global;

/**
 *
 * @author Marek
 */
public class SpeedRestriction {
  
  public enum eDirection{
    atMost,
    atLeast,
    exactly
  }
  
  public final eDirection direction;
  public final int speedInKts;

  public SpeedRestriction(eDirection direction, int speedInKts) {
    this.direction = direction;
    this.speedInKts = speedInKts;
  }
}
