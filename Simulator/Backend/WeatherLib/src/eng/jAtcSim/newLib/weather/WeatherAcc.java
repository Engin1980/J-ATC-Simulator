package eng.jAtcSim.newLib.weather;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

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
