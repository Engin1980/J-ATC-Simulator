package eng.jAtcSim.lib.weathers;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;

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
