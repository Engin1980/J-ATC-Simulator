package eng.jAtcSim.lib.global.newSources;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.DynamicWeatherProvider;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;

public class OnlineWeatherSource extends WeatherSource {
  private boolean refreshOnInit;
  @XmlIgnore
  private WeatherProvider content;

  public OnlineWeatherSource(boolean refreshOnInit) {
    this.refreshOnInit = refreshOnInit;
  }

  @Override
  public void init() {
    content = new DynamicWeatherProvider(new MetarDownloaderNoaaGov());
    if (refreshOnInit)
      ((DynamicWeatherProvider) content).getNewWeather();
    super.setInitialized();
  }

  @Override
  protected WeatherProvider _getContent() {
    return content;
  }
}
