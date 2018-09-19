package eng.jAtcSim.lib.weathers;

public class StaticWeatherProvider extends WeatherProvider {

  private Weather initialWeather;

  public StaticWeatherProvider(Weather weather) {
    this.initialWeather = weather;
  }

  @Override
  public Weather tryGetNewWeather() {
    Weather ret = initialWeather;
    if (initialWeather != null) initialWeather = null;
    return ret;
  }
}
