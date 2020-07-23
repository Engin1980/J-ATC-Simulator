package eng.jAtcSim.newLib.weather.context;

import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class WeatherContext implements IWeatherContext {

  private final WeatherManager weatherManager;

  public WeatherContext(WeatherManager weatherManager) {
    this.weatherManager = weatherManager;
  }

  @Override
  public WeatherManager getWeatherManager() {
    return this.weatherManager;
  }

}
