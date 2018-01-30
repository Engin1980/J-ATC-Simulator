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
public class Global {
  /**
   * If TRUE, Coordinates are displayed as DD°MM'SS", if FALSE, as decimal.
   */
  public static boolean COORDINATE_LONG = false;
  
  public static double MAX_ARRIVING_PLANE_DISTANCE = 15;

  public static <T> T as (Object obj){
    T ret;
    try{
      ret = (T) obj;
    } catch (Throwable ex){
      ret = null;
    }
    return ret;
  }
}