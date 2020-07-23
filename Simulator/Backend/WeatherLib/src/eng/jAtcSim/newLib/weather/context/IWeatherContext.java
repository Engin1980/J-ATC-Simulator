package eng.jAtcSim.newLib.weather.context;

import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherManager;

public interface IWeatherContext {
  WeatherManager getWeatherManager();
default  Weather getWeather(){
  return getWeatherManager().getWeather();
}
}
