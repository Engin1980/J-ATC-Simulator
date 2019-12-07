package eng.jAtcSim.newLib.global.newSources;

import eng.eSystem.validation.Validator;
;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.weathers.StaticWeatherProvider;
import eng.jAtcSim.newLib.weathers.Weather;
import eng.jAtcSim.newLib.weathers.WeatherProvider;

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
