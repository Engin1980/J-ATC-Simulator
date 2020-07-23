package eng.jAtcSim.newLib.weather.context;

import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherManager;

public interface IWeatherAcc {
  WeatherManager getWeatherManager();
default  Weather getWeather(){
  return getWeatherManager().getWeather();
}
}
