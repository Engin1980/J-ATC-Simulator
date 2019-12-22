package eng.jAtcSim.newLib.area.weathers;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;

public class StaticWeatherProvider extends WeatherProvider {

  @XmlIgnore
  private boolean wasReturned = false;
  private Weather weather;

  public StaticWeatherProvider(Weather weather) {
    this.weather = weather;
  }

  @Override
  public Weather tryGetNewWeather() {
    if (wasReturned) return null;
    wasReturned = true;
    return weather;
  }
}
