package eng.jAtcSim.newLib.global.newSources;

import eng.eSystem.validation.Validator;
;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.weathers.DynamicWeatherProvider;
import eng.jAtcSim.newLib.weathers.Weather;
import eng.jAtcSim.newLib.weathers.WeatherProvider;
import eng.jAtcSim.newLib.weathers.downloaders.MetarDownloaderNoaaGov;

public class OnlineWeatherSource extends WeatherSource {
  private String icao;
  private Weather defaultWeather;
  @XmlIgnore
  private WeatherProvider content;

  @XmlConstructor
  private OnlineWeatherSource(){
  }

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
