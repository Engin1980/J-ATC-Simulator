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
public final class EMath {

  public static int down(double value) {
    if (value > 0) {
      return (int) Math.floor(value);
    } else {
      return (int) Math.ceil(value);
    }
  }
}
