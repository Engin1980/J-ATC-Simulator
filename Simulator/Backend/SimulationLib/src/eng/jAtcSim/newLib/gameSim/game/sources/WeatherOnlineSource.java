package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.eSystem.validation.EAssertException;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.weather.DynamicWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloaderNoaaGov;

public class WeatherOnlineSource extends WeatherSource {
  private final String icao;
  private final Weather defaultWeather;
  private WeatherProvider content;

  public WeatherOnlineSource(boolean refreshOnInit, String icao, Weather defaultWeather) {
    EAssert.matchPattern(icao, "^[A-Z]{4}$");
//    this.refreshOnInit = refreshOnInit;
    this.icao = icao;
    this.defaultWeather = defaultWeather;
  }

  @Override
  public void init() {
    content = new DynamicWeatherProvider(new MetarDownloaderNoaaGov(), icao, defaultWeather, true);
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
