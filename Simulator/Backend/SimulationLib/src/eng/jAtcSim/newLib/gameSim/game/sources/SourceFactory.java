package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.weather.Weather;

public class SourceFactory {
  public static AreaSource createAreaSource(String fileName, String icao) {
    return new AreaSource(fileName, icao);
  }

  public static WeatherSource createWeatherXmlSource(String fileName) {
    return new WeatherXmlSource(fileName);
  }

  public static WeatherSource createWeatherUserSource(Weather weather) {
    return new WeatherUserSource(weather);
  }

  public static TrafficSource createTrafficXmlSource(String fileName){
    return new TrafficXmlSource(fileName);
  }

  public static WeatherSource createWeatherOnlineSource(String icao, Weather fallbackWeather){
    return new WeatherOnlineSource(true, icao, fallbackWeather);
  }

  public static TrafficSource createTrafficUserSource(ITrafficModel userTrafficModel){
    return new TrafficUserSource(userTrafficModel);
  }

  public static FleetsSource createFleetsSource (String generalAviationFileName, String companyFileName){
    return new FleetsSource(generalAviationFileName, companyFileName);
  }

  public static AirplaneTypesSource createAirplaneTypesSource(String fileName){
    return new AirplaneTypesSource(fileName);
  }
}
