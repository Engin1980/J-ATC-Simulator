package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class WeatherModule {
  private final WeatherManager weatherManager;
  public WeatherModule(WeatherManager weatherManager) {
    EAssert.Argument.isNotNull(weatherManager, "weatherManager");

    this.weatherManager = weatherManager;
  }
}
