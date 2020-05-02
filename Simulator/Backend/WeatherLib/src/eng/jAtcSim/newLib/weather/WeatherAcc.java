package eng.jAtcSim.newLib.weather;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class WeatherAcc {
  private static Producer<Weather> weatherProducer = null;

  public static void setWeatherProducer(Producer<Weather> weatherProducer) {
    WeatherAcc.weatherProducer = weatherProducer;
  }

  public static Weather getWeather(){
    return weatherProducer.produce();
  }
}
