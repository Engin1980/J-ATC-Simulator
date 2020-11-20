package eng.jAtcSim.newLib.weather.context;

import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public class WeatherAcc implements IWeatherAcc {

  private final WeatherProvider weatherProvider;
  private final EventAnonymousSimple onWeatherUpdated = new EventAnonymousSimple();
  private Weather weather;

  public WeatherAcc(WeatherProvider weatherProvider) {
    EAssert.Argument.isNotNull(weatherProvider, "weatherProvider");
    this.weatherProvider = weatherProvider;

    PostContracts.register(this, () -> weather != null);
  }

  @Override
  public Weather getWeather() {
    return this.weather;
  }

  public void setWeather(Weather weather) {
    if (weather != this.weather) {
      this.weather = weather;
      this.onWeatherUpdated.raise();
    }
  }

  @Override
  public WeatherProvider getWeatherProvider() {
    return weatherProvider;
  }

  @Override
  public EventAnonymousSimple onWeatherUpdated() {
    return onWeatherUpdated;
  }
}
