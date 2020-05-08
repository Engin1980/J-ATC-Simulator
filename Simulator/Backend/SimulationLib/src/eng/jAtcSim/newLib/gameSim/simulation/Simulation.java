package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcAcc;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.*;
import eng.jAtcSim.newLib.gameSim.simulation.modules.AirplanesSimModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.ISimulationModuleParent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.stats.StatsAcc;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.IParser;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherAcc;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class Simulation {

  public class SimulationModuleParent implements ISimulationModuleParent {

    @Override
    public AirplanesController getAirplanesController() {
      return Simulation.this.airplanesController;
    }

    @Override
    public AirproxController getAirproxController() {
      return Simulation.this.airproxController;
    }

    @Override
    public SimulationContext getContext() {
      return Simulation.this.context;
    }

    @Override
    public EmergencyAppearanceController getEmergencyAppearanceController() {
      return Simulation.this.emergencyAppearanceController;
    }

    @Override
    public MrvaController getMrvaController() {
      return Simulation.this.mrvaController;
    }

    @Override
    public TrafficProvider getTrafficProvider() {
      return Simulation.this.trafficProvider;
    }
  }

  private static final boolean DEBUG_STYLE_TIMER = false;
  private final SimulationContext context;
  private final SimulationSettings settings;
  private final EDayTimeRun now;
  private final AtcProvider atcProvider;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final AirproxController airproxController;
  private final StatsProvider statsProvider;
  private final AirplanesController airplanesController = new AirplanesController();
  private final WeatherManager weatherManager;
  private final TrafficProvider trafficProvider;
  private final TimerController timer;
  private final SystemMessagesController systemMessagesProcessor = new SystemMessagesController();
  private final IParser parser;
  private final Formatter formatter;
  private final AirplanesSimModule airplanesSimModule;
  private boolean isElapseSecondCalculationRunning = false;

  public Simulation(
      SimulationContext simulationContext,
      SimulationSettings simulationSettings) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");
    ETimeStamp simulationStartTime = simulationSettings.simulationSettings.startTime;
    this.context = simulationContext;
    this.settings = simulationSettings;
    this.now = new EDayTimeRun(simulationStartTime.getValue());


    this.atcProvider = new AtcProvider(context.getActiveAirport());
    this.trafficProvider = new TrafficProvider(context.getTraffic());
    this.emergencyAppearanceController = new EmergencyAppearanceController(
        simulationSettings.trafficSettings.emergencyPerDayProbability);
    this.mrvaController = new MrvaController(
        context.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva));
    this.airproxController = new AirproxController();
    this.statsProvider = new StatsProvider(settings.simulationSettings.statsSnapshotDistanceInMinutes);
    this.weatherManager = new WeatherManager(simulationContext.getWeatherProvider());

    this.parser = settings.parser;
    this.formatter = settings.formatter;

    this.airplanesSimModule = new AirplanesSimModule(
        this.new SimulationModuleParent(),
        simulationSettings.trafficSettings.trafficDelayStepProbability,
        simulationSettings.trafficSettings.trafficDelayStep,
        simulationSettings.trafficSettings.useExtendedCallsigns);

    this.timer = new TimerController(settings.simulationSettings.secondLengthInMs, this::timerTicked);
  }

  public void init() {
    this.airplanesController.init();
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

    // airplanes stuff
    airplanesSimModule.elapseSecond();

    // atc stuff
    atcProvider.elapseSecond();

    // stats here

    // weather
    WeatherAcc.getWeatherManager().elapseSecond();
    if (WeatherAcc.getWeatherManager().isNewWeather()) {
      this.atcProvider.adviceWeatherUpdated();
      sendTextMessageForUser("Weather updated: " + this.weatherManager.getWeather().toInfoString());
    }

    // finalize
    long elapseEndMs = System.currentTimeMillis();
    StatsAcc.getStatsProvider().registerElapseSecondDuration((int) (elapseEndMs - elapseStartMs));

    isElapseSecondCalculationRunning = false;

    // raises event
//    this.onSecondElapsed.raise();

    if (DEBUG_STYLE_TIMER)
      this.timer.start();
  }

  private void sendTextMessageForUser(String text) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(AtcAcc.getAtcList().getFirst(q -> q.getType() == AtcType.app)),
        new StringMessageContent(text));
    MessagingAcc.getMessenger().send(m);
  }

  private void timerTicked(TimerController sender) {
    elapseSecond();
  }
}
