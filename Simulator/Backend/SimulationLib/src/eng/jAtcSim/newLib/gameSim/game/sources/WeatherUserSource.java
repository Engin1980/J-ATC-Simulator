package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.weather.StaticWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;

import exml.annotations.XConstructor;

public class WeatherUserSource extends WeatherSource {
  private final Weather initialWeather;

  @XConstructor
  private WeatherUserSource() {
    initialWeather = null;
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
