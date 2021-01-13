package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.weather.DynamicWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.jAtcSim.newLib.weather.downloaders.MetarDownloaderNoaaGov;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class WeatherOnlineSource extends WeatherSource {
  private final String icao;
  private final Weather fallbackWeather;

  @XConstructor
  @XmlConstructor
  private WeatherOnlineSource() {
    icao = null;
    fallbackWeather = null;

    PostContracts.register(this, () -> icao != null);
    PostContracts.register(this, () -> fallbackWeather != null);
  }

  public WeatherOnlineSource(boolean refreshOnInit, String icao, Weather fallbackWeather) {
    EAssert.matchPattern(icao, "^[A-Z]{4}$");
    this.icao = icao;
    this.fallbackWeather = fallbackWeather;
  }

  public Weather getFallbackWeather() {
    return fallbackWeather;
  }

  public String getIcao() {
    return icao;
  }

  @Override
  protected void _init() {
    WeatherProvider content = new DynamicWeatherProvider(new MetarDownloaderNoaaGov(), icao, fallbackWeather, true);
    super.setContent(content);
  }
}
