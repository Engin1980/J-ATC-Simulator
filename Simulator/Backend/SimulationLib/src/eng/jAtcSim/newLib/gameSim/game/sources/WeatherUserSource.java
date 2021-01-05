package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.weather.StaticWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.newXmlUtils.annotations.XmlConstructor;

public class WeatherUserSource extends WeatherSource {
  private final Weather initialWeather;

  @XmlConstructor
  public WeatherUserSource() {
    this.initialWeather = null;
    PostContracts.register(this, () -> initialWeather != null, "initialWeather");
  }

  WeatherUserSource(Weather weather) {
    EAssert.Argument.isNotNull(weather, "weather");
    this.initialWeather = weather;
  }

  public Weather getInitialWeather() {
    return initialWeather;
  }

  @Override
  protected void _init() {
    WeatherProvider content = new StaticWeatherProvider(this.initialWeather);
    super.setContent(content);
  }
}
