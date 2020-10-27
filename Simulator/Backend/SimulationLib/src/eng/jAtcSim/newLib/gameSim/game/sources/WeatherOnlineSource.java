package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.DynamicWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloaderNoaaGov;

public class WeatherOnlineSource extends WeatherSource {
  private final String icao;
  private final Weather fallbackWeather;
  private WeatherProvider content;

  public WeatherOnlineSource(boolean refreshOnInit, String icao, Weather fallbackWeather) {
    EAssert.matchPattern(icao, "^[A-Z]{4}$");
    this.icao = icao;
    this.fallbackWeather = fallbackWeather;
  }

  public String getIcao() {
    return icao;
  }

  public Weather getFallbackWeather() {
    return fallbackWeather;
  }

  @Override
  public void init() {
    content = new DynamicWeatherProvider(new MetarDownloaderNoaaGov(), icao, fallbackWeather, true);
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
