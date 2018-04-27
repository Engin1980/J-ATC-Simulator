package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;

public abstract class WeatherProvider {
  private Weather weather;
  @XmlIgnore
  private EventAnonymousSimple weatherUpdatedEvent = new EventAnonymousSimple();

  public EventAnonymousSimple getWeatherUpdatedEvent() {
    return weatherUpdatedEvent;
  }

  public Weather getWeather() {
    return weather;
  }

  public void setWeather(Weather weather) {
    assert weather != null;
    this.weather = weather;
    weatherUpdatedEvent.raise();
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

  public abstract void elapseSecond();
}
