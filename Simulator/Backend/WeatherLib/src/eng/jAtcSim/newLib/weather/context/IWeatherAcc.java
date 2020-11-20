package eng.jAtcSim.newLib.weather.context;

import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherManager;
import eng.jAtcSim.newLib.weather.WeatherProvider;

public interface IWeatherAcc {
  WeatherProvider getWeatherProvider();
  Weather getWeather();
  void setWeather(Weather weather);

  EventAnonymousSimple onWeatherUpdated();
}
