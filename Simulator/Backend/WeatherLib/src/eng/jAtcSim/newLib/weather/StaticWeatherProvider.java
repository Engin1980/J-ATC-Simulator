package eng.jAtcSim.newLib.weather;

public class StaticWeatherProvider extends WeatherProvider {

  private boolean wasReturned = false;
  private Weather weather;

  public StaticWeatherProvider(Weather weather) {
    this.weather = weather;
  }

  @Override
  public Weather tryGetNewWeather() {
    if (wasReturned) return null;
    wasReturned = true;
    return weather;
  }
}
