package eng.jAtcSim.newLib.weather.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherManager;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class WeatherAcc {
  private static Producer<Weather> weatherProducer;
  private static Producer<WeatherManager> weatherManagerProducer = null;

  public static WeatherManager getWeatherManager() {
    return weatherManagerProducer.produce();
  }

  public static void setWeatherManagerProducer(Producer<WeatherManager> weatherManagerProducer) {
    WeatherAcc.weatherManagerProducer = weatherManagerProducer;
    WeatherAcc.weatherProducer = () -> weatherManagerProducer.produce().getWeather();
  }


  public static Weather getWeather(){
    return weatherProducer.produce();
  }
}