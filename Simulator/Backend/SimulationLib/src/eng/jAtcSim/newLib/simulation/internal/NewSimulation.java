package eng.jAtcSim.newLib.simulation.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.simulation.TimerProvider;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.Parser;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class NewSimulation {
  private static final boolean DEBUG_STYLE_TIMER = false;
  private final SimulationContext context;
  private final SimulationSettings settings;
  private final EDayTimeRun now;
  private final AtcProvider atcProvider;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final StatsProvider statsProvider;
  private final AirplanesController airplanesController = new AirplanesController();
  private final WeatherManager weatherManager;
  private final TrafficProvider trafficProvider;
  private final TimerProvider timer;
  private final SystemMessagesProcessor systemMessagesProcessor = new SystemMessagesProcessor();
  private final Parser parser;
  private final Formatter formatter;
  private boolean isElapseSecondCalculationRunning = false;

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

    this.parser = settings.getParser();
    this.formatter = settings.getFormatter();

    this.timer = new TimerProvider(settings.getSimulationSecondLengthInMs(), this::timerTicked);
  }

  public void init() {
    this.weatherManager.init();
    this.atcProvider.init();
    this.statsProvider.init();
    this.trafficProvider.init();
  }

  private void elapseSecond() {
    long elapseStartMs = System.currentTimeMillis();

    if (isElapseSecondCalculationRunning) {
      SharedAcc.getAppLog().writeLine(
          ApplicationLog.eType.warning,
          "elapseSecond() called before the previous one was finished!");
      return;
    }
    if (DEBUG_STYLE_TIMER)
      timer.stop();
    isElapseSecondCalculationRunning = true;
    now.increaseSecond();

    // process system messages
    systemMessagesProcessor.elapseSecond();

    // traffic stuff
    IReadOnlyList<MovementTemplate> newMovements = trafficProvider.getMovementsUntilTime(SharedAcc.getNow());
    airplanesController.registerPlanes
    trafficManager.generateNewTrafficIfRequired();
  }

  private void timerTicked(TimerProvider sender) {
    elapseSecond();
  }
}
