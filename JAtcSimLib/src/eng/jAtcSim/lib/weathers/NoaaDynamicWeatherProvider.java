package eng.jAtcSim.lib.weathers;

import eng.jAtcSim.lib.Timer;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloader;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;

public class NoaaDynamicWeatherProvider extends DynamicWeatherProvider {

  private static final int UPDATE_INTERVAL_IN_REAL_TIME_SECONDS = 60 * 5;
  private String icao;
  private Timer timer;

  private static Weather downloadAndDecodeMetar(String icao) {
    Weather ret;
    MetarDownloader downloader = new MetarDownloaderNoaaGov();
    String metarText = downloader.downloadMetar(icao);
    ret = decodeMetar((metarText));
    return ret;
  }

  private static Weather decodeMetar(String metar) {
    Weather ret = MetarDecoder.decode(metar);
    return ret;
  }

  public NoaaDynamicWeatherProvider(String icao) {
    this.icao = icao;
    this.timer = new Timer(q -> tmr_tick());
    this.timer.start(UPDATE_INTERVAL_IN_REAL_TIME_SECONDS * 1000); // every 5 minutes
  }

  private void tmr_tick() {
    super.updateWeather(true);
  }

  @Override
  protected Weather getUpdatedWeather() {
    Weather ret;
    try {
      ret = downloadAndDecodeMetar(this.icao);
    } catch (Exception ex) {
      throw ex;
    }
    return ret;
  }
}