/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

/**
 *
 * @author Marek
 */
public class Restriction {
  
  public enum eDirection{
    atMost,
    atLeast,
    exactly
  }
  
  public final eDirection direction;
  public final int value;

  public Restriction(eDirection direction, int value) {
    this.direction = direction;
    this.value = value;
  }

  @Override
  public String toString() {
    return direction + " " + value;
  }
  
}