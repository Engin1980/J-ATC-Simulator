/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.weathers.decoders;

import com.sun.javafx.image.BytePixelSetter;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marek Vajgl
 */
public class MetarDecoder {

  /**
   * Tries to decode metar string into Weather object.
   *
   * @param metarLine Metar string. Should include "METAR" prefix.
   * @return Decoded weather, or exception description.
   */
  public static PresetWeather decode(String metarLine) {
    PresetWeather ret;

    metarLine = cutOutPostfix(metarLine); // cut out TEMPO/BECMG
    try {
      ret = decodeWeather(metarLine);
    } catch (Exception ex) {
      throw new ERuntimeException("Failed to decode metar from string " + metarLine + ".", ex);
    }

    return ret;
  }

  private static PresetWeather decodeWeather(String metarLine) {
    LocalTime time = decodeTime(metarLine);
    int[] wind = decodeWindDirSpeedGusts(metarLine);
    int visibilityInM = decodeVisibility(metarLine);
    CloudBaseResult cloudBaseInFt = decodeCloudBase(metarLine);
    Weather.eSnowState snowState = decodeSnowState(metarLine);

    PresetWeather w = new PresetWeather(time, wind[0], wind[1], wind[2], visibilityInM, cloudBaseInFt.altitudeInFt, cloudBaseInFt.probability, snowState);

    return w;
  }

  private static Weather.eSnowState decodeSnowState(String metarLine) {
    if (RegexUtils.isMatch(metarLine, "\\+[A-Z]*SN "))
      return Weather.eSnowState.intensive;
    else if (RegexUtils.isMatch(metarLine, "SN "))
      return Weather.eSnowState.normal;
    else
      return Weather.eSnowState.none;
  }

  private static int[] decodeWindDirSpeedGusts(String metarLine) {
    int[] ret = new int[3];
    String pattern = "(\\d{3}|VRB)(\\d{2})(G(\\d{2}))?(KT|MPS)";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(metarLine);

    if (m.find()) {
      Integer direction;
      Integer speed;
      Integer gusts;

      if (m.group(1).equals("VRB")) {
        direction = 0;
      } else {
        direction = getIntFromGroup(m, 1);
      }

      speed = getIntFromGroup(m, 2);
      gusts = getIntFromGroup(m, 4);
      if (m.group(5).equals("MPS")) {
        speed = (int) (speed * 1.9438444924406046);
        if (gusts != null) {
          gusts = (int) (gusts * 1.9438444924406046);
        }
      }

      ret[0] = direction;
      ret[1] = speed;
      ret[2] = gusts != null ? gusts : speed;
    }

    return ret;
  }

  private static Integer getIntFromGroup(Matcher m, int groupIndex) {
    Integer ret = null;
    String val = m.group(groupIndex);
    if (val != null) {
      ret = Integer.parseInt(val);
    }
    return ret;
  }

  private static int decodeVisibility(String metarLine) {
    int ret = 9999;
    String pattern = " (\\d{4}) ";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(metarLine);

    if (m.find()) {
      ret = getIntFromGroup(m, 1);
    }

    return ret;
  }

  private static LocalTime decodeTime(String metarLine) {
    LocalTime ret;
    int hour;
    int minute;
    String pattern = " (\\d{2})(\\d{2})(\\d{2})Z ";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(metarLine);

    if (m.find()) {
      hour = getIntFromGroup(m, 2);
      minute = getIntFromGroup(m, 3);
      ret = LocalTime.of(hour, minute);
    } else
      throw new IllegalArgumentException("Unable to decode time from " + metarLine);

    return ret;
  }

  private static CloudBaseResult decodeCloudBase(String metarLine) {
    CloudBaseResult ret = new CloudBaseResult();
    ret.altitudeInFt = 10000;
    ret.probability = 0;

    String[] nones = new String[]{"SKC", "NSC", "NCD"};
    boolean isNone = false;
    for (String none : nones) {
      if (metarLine.contains(none)) {
        isNone = true;
        break;
      }
    }

    if (isNone) {
      return ret;
    }

    String pattern = "((FEW)|(SCT)|(BKN)|(OVC)|(VV))(\\d{3})";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(metarLine);

    while (m.find()) {
      int alt = getIntFromGroup(m, 7);
      double prob = decodeVisibilityProbability(m);
      if (prob > ret.probability) {
        ret.probability = prob;
        ret.altitudeInFt = alt * 100;
      }
    }

    return ret;
  }

  private static String cutOutPostfix(String metarLine) {
    int cutIndex;

    cutIndex = metarLine.indexOf("TEMPO");
    if (cutIndex > -1) {
      metarLine = metarLine.substring(0, cutIndex);
    }

    cutIndex = metarLine.indexOf("BECMG");
    if (cutIndex > -1) {
      metarLine = metarLine.substring(0, cutIndex);
    }

    return metarLine;
  }

  private static double decodeVisibilityProbability(Matcher m) {
    double ret = 0;
    String tmp = m.group(1);
    if (tmp.equals("FEW")) {
      return 0.25;
    } else if (tmp.equals("SCT")) {
      return 0.5;
    } else if (tmp.equals("BKN")) {
      return 0.75;
    } else if (tmp.equals("OVC")) {
      return 1d;
    } else if (tmp.equals("VV")) {
      return 1d;
    } else {
      throw new RuntimeException("This modifier of visibility is not supported.");
    }
  }
}

class CloudBaseResult {

  public int altitudeInFt;
  public double probability;
}
