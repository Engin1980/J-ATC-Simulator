/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.weathers;

import eng.jAtcSim.lib.exceptions.ERuntimeException;
import jatcsimlib.exceptions.ERuntimeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek
 */
public class MetarDownloaderNoaaGov extends MetarDownloader {

  @Override
  protected String downloadMetarString(String icao) {
    String url = 
        String.format(
            // "http://weather.noaa.gov/pub/data/observations/metar/stations/%1$s.TXT", 
                "ftp://tgftp.nws.noaa.gov/data/observations/metar/stations/%1$s.TXT",
            icao.toUpperCase());
    
    BufferedReader br = super.getBufferedReader(url);
    
    String line;
    
    try{
    line = br.readLine(); // skips the first line, where date is
    line = br.readLine();
    } catch (IOException ex ){
      throw new ERuntimeException("Failed to read content of url " + url);
    } finally {
      try {
        br.close();
      } catch (IOException ex) {
      }
    }
    
    return "METAR " + line;
  }

}
