package eng.jAtcSim.newLib.weather;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.shared.SharedFactory;
import eng.jAtcSim.newLib.weather.decoders.MetarDecoder;

public class WeatherManager {
  private Weather currentWeather;
  private boolean newWeatherFlag;
  private WeatherProvider provider;

  public WeatherManager(WeatherProvider provider) {
    Validator.isNotNull(provider);

    this.provider = provider;
    this.newWeatherFlag = true;
  }

  public void elapseSecond() {
    Weather newWeather = provider.tryGetNewWeather();
    if (newWeather != null) {
      synchronized (provider) {
        currentWeather = newWeather;
        newWeatherFlag = true;
      }
    }
  }

  public Weather getWeather() {
    synchronized (provider) {
      return currentWeather;
    }
  }

  public void init() {
    this.currentWeather = provider.tryGetNewWeather();
    assert this.currentWeather != null;
  }

  public boolean isNewWeatherFlagAndResetIt() {
    boolean ret = newWeatherFlag;
    if (newWeatherFlag) newWeatherFlag = false;
    return ret;
  }

  public void setWeather(String metarString) {
    try {
      Weather tmp = MetarDecoder.decode(metarString);
      setWeather(tmp);
    } catch (Exception ex) {
      SharedFactory.getSimLog().sendTextMessageForUser("Failed to decode metar. " + ex.getMessage());
    }
  }

  public void setWeather(Weather weather) {
    Validator.isNotNull(weather);
    synchronized (provider) {
      this.currentWeather = weather;
      this.newWeatherFlag = true;
    }
  }
}
