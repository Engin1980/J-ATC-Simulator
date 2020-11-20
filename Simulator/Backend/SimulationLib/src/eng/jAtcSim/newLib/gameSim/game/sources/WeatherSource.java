package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;
import eng.jAtcSim.newLib.weather.context.WeatherAcc;

public abstract class WeatherSource extends Source<WeatherProvider> {
  protected abstract void _init();

  public void init(){
    _init();
    IWeatherAcc weatherAcc = new WeatherAcc(this.getContent());
    ContextManager.setContext(IWeatherAcc.class, weatherAcc);
  }
}
