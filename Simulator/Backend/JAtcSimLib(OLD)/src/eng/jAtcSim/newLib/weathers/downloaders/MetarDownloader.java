/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area.weathers.downloaders;

import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.ApplicationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Marek
 */
public abstract class MetarDownloader {

  /**
   * Downloads metar from the internet.
   * @param icao ICAO of the airport to download metar of
   * @return Metar string, or exception.
   */
  public String downloadMetar(String icao) {
    String ret;

    try {
      ret = downloadMetarString(icao);
    } catch (Exception ex) {
      throw new ApplicationException("Failed to download metar.", ex);
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
      throw new ApplicationException("Cannot open reader to URL: " + urlString + " cos it is not valid.", ex);
    }
    try {
      is = url.openStream();
    } catch (IOException ex) {
      throw new ApplicationException("Failed to open stream to " + urlString + ".",ex);
    }

    ret = new BufferedReader(new InputStreamReader(is));

    return ret;
  }

  protected abstract String downloadMetarString(String icao);
}
