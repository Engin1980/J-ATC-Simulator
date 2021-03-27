package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;
import eng.jAtcSim.newLib.weather.WeatherManager;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;
import eng.jAtcSim.newLib.weather.context.WeatherAcc;

import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;

public class WeatherModule extends SimulationModule {
  private final WeatherManager weatherManager;

  @XConstructor
  public WeatherModule(XLoadContext ctx) {
    super(ctx);
    this.weatherManager = null;
  }

  public WeatherModule(Simulation parent, WeatherManager weatherManager) {
    super(parent);
    EAssert.Argument.isNotNull(weatherManager, "weatherManager");

    this.weatherManager = weatherManager;
  }

  public void elapseSecond() {
    weatherManager.elapseSecond();
    if (weatherManager.isNewWeather()) {
      Context.getWeather().setWeather(weatherManager.getWeather());
    }
  }

  public void init() {
    weatherManager.init();
  }
}
