package eng.jAtcSim.newLib.gameSim.game.sources;

import eng.jAtcSim.newLib.weather.WeatherProvider;

public abstract class WeatherSource extends Source<WeatherProvider> {
  public abstract void init();
}
