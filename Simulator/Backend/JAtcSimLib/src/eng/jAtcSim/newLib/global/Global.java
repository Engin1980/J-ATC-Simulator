/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.global;

import eng.eSystem.utilites.ConversionUtils;

/**
 * @author Marek
 */
public class Global {
  public static final int MINIMUM_ATC_SPEECH_DELAY_SECONDS = 3;
  public static final int MAXIMUM_ATC_SPEECH_DELAY_SECONDS = 25;
  public static final boolean WEATHER_INFO_STRING_AS_METAR = true;

  /**
   * If TRUE, Coordinates are displayed as DD°MM'SS", if FALSE, as decimal.
   */
  public static boolean COORDINATE_LONG = false;

  public final static int REPEATED_RADAR_CONTACT_REQUEST_SECONDS = 20;
  public final static int REPEATED_SWITCH_REQUEST_SECONDS = 20;


  public static <T> T as(Object obj, Class<T> type) {
    T ret = ConversionUtils.tryConvert(obj, type);
    return ret;
  }
}
