package eng.jAtcSim.newLib.area.weathers;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.weathers.decoders.MetarDecoder;

public class WeatherManager {
  private Weather currentWeather;
  private boolean newWeatherFlag;
  private WeatherProvider provider;

  public WeatherManager(WeatherProvider provider) {
    Validator.isNotNull(provider);

    this.provider = provider;
    this.newWeatherFlag = true;
  }

  public boolean isNewWeatherFlagAndResetIt() {
    boolean ret = newWeatherFlag;
    if (newWeatherFlag) newWeatherFlag = false;
    return ret;
  }

  public Weather getWeather() {
    synchronized (provider) {
      return currentWeather;
    }
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

  public void setWeather(Weather weather) {
    Validator.isNotNull(weather);
    synchronized (provider) {
      this.currentWeather = weather;
      this.newWeatherFlag = true;
    }
  }

  public void setWeather(String metarString) {
    try {
      Weather tmp = MetarDecoder.decode(metarString);
      setWeather(tmp);
    } catch (Exception ex) {
      Acc.sim().sendTextMessageForUser("Failed to decode metar. " + ex.getMessage());
    }
  }

  public void init() {
    this.currentWeather = provider.tryGetNewWeather();
    assert this.currentWeather != null;
  }
}
