/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.weathers;

import jatcsimlib.exceptions.ERuntimeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marek
 */
public abstract class WeatherDownloader {

  public Weather downloadWeather(String icao) {
    String m = downloadMetarString(icao);
    Weather ret = createWeather(m);
    return ret;
  }

  protected BufferedReader getBufferedReader(String urlString) {
    URL url;
    InputStream is;
    BufferedReader ret;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException ex) {
      throw new ERuntimeException("Cannot open reader to URL: " + urlString + " cos it is not valid.", ex);
    }
    try {
      is = url.openStream();
    } catch (IOException ex) {
      throw new ERuntimeException("Failed to open stream to " + urlString + ".", ex);
    }

    ret = new BufferedReader(new InputStreamReader(is));

    return ret;
  }

  protected abstract String downloadMetarString(String icao);

  public Weather createWeather(String metarString) {

    // METAR \w{4} \d{6}Z (\d{3}|VRB)(\d{2})(?:G\d{2})?([^ ]+).+?((?:\d{4})|(?:CAVOK))(?: .+? (?:BKN|OVC)(\d{3}))?
    String patternS = "METAR \\w{4} \\d{6}Z (\\d{3}|VRB)(\\d{2})(?:G\\d{2})?([^ ]+).+?((?:\\d{4})|(?:CAVOK))(?: .+? (?:BKN|OVC)(\\d{3}))?";
    Pattern p = Pattern.compile(patternS);
    Matcher m = p.matcher(metarString);

    if (m.find() == false) {
      throw new ERuntimeException("Failed to extract weather from metar " + metarString + ". Decoder: " + this.getClass().getSimpleName());
    }

    int windDir;
    int windSpd;
    String windSpdUnit;
    int visibility;
    int cloudBase;

    if ("VRB".equals(m.group(1))) {
      windDir = 0;
    } else {
      windDir = toInt(m.group(1));
    }
    windSpd = toInt(m.group(2));
    windSpdUnit = m.group(3);
    if ("CAVOK".equals(m.group(4))) {
      visibility = 10000;
    } else {
      visibility = toInt(m.group(4));
    }
    if (m.group(5) == null) {
      cloudBase = 100;
    } else {
      cloudBase = toInt(m.group(5));
    }

    if ("MPS".equals(windSpdUnit)) {
      windSpd = (int) (1.9438444924406046 * windSpd);
    }

    Weather w = new Weather(windSpd, windSpd, visibility, cloudBase * 100);

    return w;
  }

  private static int toInt(String value) {
    return Integer.parseInt(value);
  }
}
