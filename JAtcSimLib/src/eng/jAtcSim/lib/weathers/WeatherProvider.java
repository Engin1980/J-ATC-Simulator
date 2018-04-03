package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventAnonymousSimple;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;

public abstract class WeatherProvider {
  private static final boolean EXCEPTION_ON_DECODE_FAIL = true;
  private Weather weather;
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
