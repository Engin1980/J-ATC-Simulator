package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;
import eng.jAtcSim.newLib.weather.WeatherManager;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;
import eng.jAtcSim.newLib.weather.context.WeatherAcc;

public class WeatherModule extends SimulationModule {
  private final WeatherManager weatherManager;

  public WeatherModule(Simulation parent, WeatherManager weatherManager) {
    super(parent);
    EAssert.Argument.isNotNull(weatherManager, "weatherManager");

    this.weatherManager = weatherManager;
  }

  public void elapseSecond() {
    weatherManager.elapseSecond();
    if (weatherManager.isNewWeather()) {
      parent.getAtcModule().adviceWeatherUpdated();
      parent.getAtcModule().getUserAtcIds().forEach(q ->
              parent.getIoModule().sendTextMessageForUser(q, new MetarNotification(true)));
    }
  }

  public void init() {
    IWeatherAcc weatherContext = new WeatherAcc(this.weatherManager);
    ContextManager.setContext(IWeatherAcc.class, weatherContext);
    weatherManager.init();
  }
}