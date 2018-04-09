/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.eSystem.utilites.ConversionUtils;

/**
 *
 * @author Marek
 */
public class Global {
  /**
   * If TRUE, Coordinates are displayed as DD°MM'SS", if FALSE, as decimal.
   */
  public static boolean COORDINATE_LONG = false;
  

  public static <T> T as (Object obj, Class<T> type){
    T ret = ConversionUtils.tryConvert(obj, type);
    return ret;
  }
}