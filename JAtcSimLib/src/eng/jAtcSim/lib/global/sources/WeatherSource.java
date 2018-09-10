package eng.jAtcSim.lib.global.sources;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.weathers.*;

public class WeatherSource extends Source<WeatherProvider> {

  public enum ProviderType{
    staticProvider,
    dynamicNovGoaaProvider
  }

  @XmlIgnore
  private WeatherProvider provider;
  private ProviderType type;
  private String icao;
  private Weather weather;

  public WeatherSource (ProviderType type, String icao){
    this.type = type;
    this.icao = icao;
  }

  public WeatherSource(){}

  public Weather getWeather() {
    return weather;
  }

  @Override
  public WeatherProvider _get() {
    return provider;
  }

  public void init(Weather initialWeather) {

    switch (type){
      case dynamicNovGoaaProvider:
        provider = new NoaaDynamicWeatherProvider(icao);
        break;
      case staticProvider:
        provider = new StaticWeatherProvider();
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }
    provider.getOnWeatherUpdated().add(w -> this.weather = w);

    provider.setWeather(initialWeather);
    if (provider instanceof DynamicWeatherProvider)
    {
      DynamicWeatherProvider tmp = (DynamicWeatherProvider) provider;
      tmp.updateWeather(false);
    }

    super.setInitialized();
  }
}
