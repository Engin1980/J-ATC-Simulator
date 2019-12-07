package eng.jAtcSim.newLib.global.newSources;

import eng.jAtcSim.newLib.weathers.WeatherProvider;

public abstract class WeatherSource extends Source<WeatherProvider> {
  public abstract void init();
}
