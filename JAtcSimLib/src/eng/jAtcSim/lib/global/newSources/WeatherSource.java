package eng.jAtcSim.lib.global.newSources;

import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.weathers.presets.PresetWeather;

public abstract class WeatherSource extends Source<WeatherProvider> {
  public abstract void init();
}
