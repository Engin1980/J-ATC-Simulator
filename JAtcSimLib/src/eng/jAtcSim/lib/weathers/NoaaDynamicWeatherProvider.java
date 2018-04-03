package eng.jAtcSim.lib.weathers;

import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloader;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;

public class NoaaDynamicWeatherProvider extends DynamicWeatherProvider {

  private static final int UPDATE_INTERVAL_IN_REAL_TIME_SECONDS = 60*1;

  public NoaaDynamicWeatherProvider(String icao) {
    super(icao, UPDATE_INTERVAL_IN_REAL_TIME_SECONDS);
  }

  @Override
  protected Weather getUpdatedWeather() {
    Weather ret;
    try {
      ret = downloadAndDecodeMetar(super.icao);
    } catch (Exception ex){
      throw ex;
    }
    return ret;
  }

  private static Weather downloadAndDecodeMetar(String icao) {
    Weather ret;
    MetarDownloader downloader = new MetarDownloaderNoaaGov();
    String  metarText = downloader.downloadMetar(icao);
    ret = decodeMetar((metarText));
    return ret;
  }

  private static Weather decodeMetar(String metar) {
    Weather ret = MetarDecoder.decode(metar);
    return ret;
  }
}
