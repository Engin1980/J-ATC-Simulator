//package eng.jAtcSim.newLib.weather.context;
//
//import eng.eSystem.functionalInterfaces.Producer;
//import eng.jAtcSim.newLib.weather.Weather;
//import eng.jAtcSim.newLib.weather.WeatherManager;
//
//public class WeatherAcc {
//  private static Producer<Weather> weatherProducer;
//  private static Producer<WeatherManager> weatherManagerProducer = null;
//
//  public static WeatherManager getWeatherManager() {
//    return weatherManagerProducer.produce();
//  }
//
//  public static void setWeatherManagerProducer(Producer<WeatherManager> weatherManagerProducer) {
//    WeatherAcc.weatherManagerProducer = weatherManagerProducer;
//    WeatherAcc.weatherProducer = () -> weatherManagerProducer.produce().getWeather();
//  }
//
//
//  public static Weather getWeather(){
//    return weatherProducer.produce();
//  }
//}
