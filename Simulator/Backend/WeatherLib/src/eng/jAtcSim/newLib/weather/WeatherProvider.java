package eng.jAtcSim.newLib.weather;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.weathers.decoders.MetarDecoder;

public abstract class WeatherProvider {
  public abstract Weather tryGetNewWeather();

  protected Weather decodeFromMetar(String metarString){
    Weather tmp;
    try {
      tmp = MetarDecoder.decode(metarString);
    } catch (Exception ex){
      throw new EApplicationException("Failed to decode metar string from " + metarString, ex);
    }
    return tmp;
  }
}
