package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.DynamicWeatherProvider;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;

public class OnlineWeatherSource extends WeatherSource {
  private boolean refreshOnInit;
  private String icao;
  private Weather defaultWeather;
  @XmlIgnore
  private WeatherProvider content;

  public OnlineWeatherSource(boolean refreshOnInit, String icao, Weather defaultWeather) {
    Validator.matchPattern(icao, "^[A-Z]{4}$");
    this.refreshOnInit = refreshOnInit;
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
