package eng.jAtcSim.newLib.weather;

import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.contextLocal.Context;
import eng.jAtcSim.newLib.weather.decoders.MetarDecoder;

import java.util.TooManyListenersException;

public class WeatherManager {
  private Weather currentWeather;
  private boolean newWeatherFlag;
  private final WeatherProvider provider;

  public WeatherManager(WeatherProvider provider) {
    EAssert.Argument.isNotNull(provider);

    this.provider = provider;
    this.newWeatherFlag = true;
  }

  public void elapseSecond() {
    this.newWeatherFlag = false;
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

  public boolean isNewWeather() {
    return newWeatherFlag;
  }

  public void setWeather(String metarString) {
    try {
      Weather tmp = MetarDecoder.decode(metarString);
      setWeather(tmp);
    } catch (Exception ex) {
      //TODO Implement this: Implement this
      throw new ToDoException("Implement this");
      //Context.getShared().getSimLog().sendTextMessageForUser("Failed to decode metar. " + ex.getMessage());
    }
  }

  public void setWeather(Weather weather) {
    EAssert.Argument.isNotNull(weather);
    synchronized (provider) {
      this.currentWeather = weather;
      this.newWeatherFlag = true;
    }
  }
}
