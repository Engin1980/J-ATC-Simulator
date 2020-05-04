package eng.jAtcSim.newLib.simulation.internal;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class NewSimulation {

  private final SimulationContext context;
  private final SimulationSettings settings;
  private final EDayTimeRun now;
  private final AtcProvider atcProvider;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final StatsProvider statsProvider;
  private final WeatherManager weatherManager;
  private final TrafficProvider trafficProvider;

  public NewSimulation(
      SimulationContext simulationContext,
      SimulationSettings simulationSettings,
      EDayTimeStamp simulationStartTime) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");
    EAssert.Argument.isNotNull(simulationStartTime, "simulationStartTime");
    this.context = simulationContext;
    this.settings = simulationSettings;
    this.now = new EDayTimeRun(simulationStartTime.getValue());

    this.atcProvider = new AtcProvider(context.getActiveAirport());
    this.trafficProvider = new TrafficProvider(context.getTraffic());
    this.emergencyAppearanceController = new EmergencyAppearanceController(
        simulationSettings.getEmergencyPerDayProbability());
    this.mrvaController = new MrvaController(
        context.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva));
    this.statsProvider = new StatsProvider(settings.getStatsSnapshotDistanceInMinutes());
    this.weatherManager = new WeatherManager(simulationContext.getWeatherProvider());
  }

  public void init(){
    this.weatherManager.init();
    this.atcProvider.init();
    this.statsProvider.init();
    this.trafficProvider.init();
  }
}
