package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;

public abstract class WeatherProvider {
  private Weather weather;
  private EventAnonymous<Weather> weatherUpdatedEvent = new EventAnonymous<>();

  public EventAnonymous<Weather> getWeatherUpdatedEvent() {
    return weatherUpdatedEvent;
  }

  public void setWeather(Weather weather) {
    assert weather != null;
    this.weather = weather;
    weatherUpdatedEvent.raise(weather);
  }

  public Weather getWeather() {
    return weather;
  }

  public void setWeatherByMetarString(String metarString) {
    Weather tmp;
    try {
      tmp = MetarDecoder.decode(metarString);
      this.setWeather(tmp);
    } catch (Exception ex){
      Acc.sim().sendTextMessageForUser("Failed to decode metar. " + ex.getMessage());
    }
  }
}
