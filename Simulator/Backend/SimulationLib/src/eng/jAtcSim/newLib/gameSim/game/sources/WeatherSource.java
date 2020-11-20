package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;
import eng.jAtcSim.newLib.weather.context.WeatherAcc;

public abstract class WeatherSource extends Source<WeatherProvider> {
  public WeatherSource() {
    PostContracts.register(this, () -> ContextManager.getContext(IWeatherAcc.class).getWeather() != null, "Initial weather not set.");
  }

  protected abstract void _init();

  public void init() {
    _init();
    IWeatherAcc weatherAcc = new WeatherAcc(this.getContent());
    Weather initialWeather = this.getContent().tryGetNewWeather();
    weatherAcc.setWeather(initialWeather);
    ContextManager.setContext(IWeatherAcc.class, weatherAcc);
  }
}
