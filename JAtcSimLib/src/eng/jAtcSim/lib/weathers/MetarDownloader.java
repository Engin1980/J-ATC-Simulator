/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.weathers;

import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.TryResult;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.TryResult;
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
public abstract class MetarDownloader {

  /**
   * Tries to download metar from the internet.
   * @param icao ICAO of the airport to download metar of
   * @return Metar string, or exception.
   */
  public TryResult<String> tryDownloadMetar(String icao) {
    TryResult<String> ret;

    try {
      String str = downloadMetarString(icao);
      ret = new TryResult<>(str);
    } catch (Exception ex) {
      ret = new TryResult<>(null, ex);
    }

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
}
