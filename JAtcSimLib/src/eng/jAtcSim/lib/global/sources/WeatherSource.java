package eng.jAtcSim.lib.global.sources;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.*;
import eng.jAtcSim.lib.weathers.downloaders.MetarDownloaderNoaaGov;

@Deprecated
public class WeatherSource extends Source<WeatherProvider> {

  public enum ProviderType{
    staticProvider,
    dynamicProvider,
    presetProvider
  }

  @XmlIgnore
  private WeatherProvider provider;
  private ProviderType type;
  private String icao;
  private String xmlFile;

  public WeatherSource(ProviderType type, String icao, String xmlFile){
    this.type = type;
    this.icao = icao;
    this.xmlFile = xmlFile;
  }

  public WeatherSource(){}

  @Override
  public WeatherProvider _get() {
    return provider;
  }

  public void init(Weather initialWeather) {

    switch (type){
      case dynamicProvider:
        provider = new DynamicWeatherProvider(new MetarDownloaderNoaaGov());
        ((DynamicWeatherProvider) provider).getNewWeather();
        break;
      case staticProvider:
        provider = new StaticWeatherProvider(initialWeather);
        break;
      case presetProvider:
        provider = new PresetWeatherProvider(this.xmlFile);
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }
    super.setInitialized();
  }
}
