package eng.jAtcSim.lib.weathers;

import eng.eSystem.events.EventAnonymous;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.downloaders.MetarDecoder;

public abstract class WeatherProvider {
  private Weather weather;
  private EventAnonymous<Weather> onWeatherUpdated = new EventAnonymous<>();

  public EventAnonymous<Weather> getOnWeatherUpdated() {
    return onWeatherUpdated;
  }

  public void setWeather(Weather weather) {
    assert weather != null;
    this.weather = weather;
    onWeatherUpdated.raise(weather);
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
