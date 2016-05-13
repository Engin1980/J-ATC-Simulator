/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.weathers;

import jatcsimlib.global.TryResult;

/**
 *
 * @author Marek Vajgl
 */
public class WeatherProvider {
  public static Weather downloadAndDecodeMetar(String icao){
    Weather ret;
    MetarDownloader downloader = new MetarDownloaderNoaaGov();
    TryResult<String> downResult = downloader.tryDownloadMetar(icao);
    if (downResult.isSuccess == false) {
      System.out.println("Error downloading metar from " + downloader.getClass().getSimpleName() + ". Reason: " + downResult.exceptionOrNull.getMessage());
      ret = new Weather();
    } else {
      ret = decodeMetar(downResult.result);
    }
    return ret;
  }
  
  public static Weather decodeMetar (String metar){
    Weather ret;
    TryResult<Weather> decResult = MetarDecoder.tryDecode(metar);
      if (decResult.isSuccess == false) {
        System.out.println("Error decoding metar \"" + metar + "\". Reason: " + decResult.exceptionOrNull.getMessage());
        ret = new Weather();
      } else {
        ret = decResult.result;
      }
      return ret;
  }
}
