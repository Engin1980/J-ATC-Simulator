package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.validation.Validator;
;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.StaticWeatherProvider;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;

public class UserWeatherSource extends WeatherSource {
  private WeatherProvider content;

  @XmlConstructor
  private UserWeatherSource() {
  }

  public UserWeatherSource(Weather weather) {
    Validator.isNotNull(weather);
    this.content = new StaticWeatherProvider(weather);
  }

  @Override
  public void init() {
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
