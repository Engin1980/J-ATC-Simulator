package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.StaticWeatherProvider;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public class WeatherUserSource extends WeatherSource {
  private WeatherProvider content;
  private final Weather initialWeather;

  WeatherUserSource(Weather weather) {
    EAssert.Argument.isNotNull(weather, "weather");
    this.content = new StaticWeatherProvider(weather);
    this.initialWeather = weather;
  }

  public Weather getInitialWeather() {
    return initialWeather;
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
