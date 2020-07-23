package eng.jAtcSim.newLib.weather.context;

import eng.jAtcSim.newLib.weather.WeatherManager;

public class WeatherAcc implements IWeatherAcc {

  private final WeatherManager weatherManager;

  public WeatherAcc(WeatherManager weatherManager) {
    this.weatherManager = weatherManager;
  }

  @Override
  public WeatherManager getWeatherManager() {
    return this.weatherManager;
  }

}
