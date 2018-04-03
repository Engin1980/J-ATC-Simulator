package eng.jAtcSim.lib.weathers;

public class StaticWeatherProvider extends WeatherProvider {

  public StaticWeatherProvider(String metarString) {
    super.setWeatherByMetarString(metarString);
  }

  @Override
  public void elapseSecond() {

  }
}
