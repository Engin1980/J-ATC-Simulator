package eng.jAtcSim.newLib.area.weathers;

import eng.eSystem.exceptions.ApplicationException;
import eng.jAtcSim.newLib.weathers.decoders.MetarDecoder;

public abstract class WeatherProvider {
  public abstract Weather tryGetNewWeather();

  protected Weather decodeFromMetar(String metarString){
    Weather tmp;
    try {
      tmp = MetarDecoder.decode(metarString);
    } catch (Exception ex){
      throw new ApplicationException("Failed to decode metar string from " + metarString, ex);
    }
    return tmp;
  }
}
