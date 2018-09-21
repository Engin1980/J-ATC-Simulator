package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.weathers.StaticWeatherProvider;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;

public class UserWeatherSource extends WeatherSource {
  private WeatherProvider content;

  public UserWeatherSource(Weather weather) {
    Validator.isNotNull(weather);
    this.content = new StaticWeatherProvider(weather);
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }

  @Override
  public void init() {
super.setInitialized();
  }
}
