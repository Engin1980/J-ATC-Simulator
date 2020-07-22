package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.*;
import eng.jAtcSim.newLib.gameSim.simulation.modules.AirplanesSimModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.ISimulationModuleParent;
import eng.jAtcSim.newLib.gameSim.simulation.modules.SystemMessagesModule;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.context.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class Simulation {

  public ISimulation isim;

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
    public IOController getIO() {
      return Simulation.this.ioController;
    }

    @Override
    public MrvaController getMrvaController() {
      return Simulation.this.mrvaController;
    }

    @Override
    public SimulationController getSimulation() {
      //TODO Implement this: How this will be implemented?
      throw new ToDoException("How this will be implemented?");
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
  private final SystemMessagesModule systemMessagesProcessor;
  private final ParserFormatterStartInfo parseFormat;
  private final IOController ioController;
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
    this.ioController = new IOController();
    this.statsProvider = new StatsProvider(settings.simulationSettings.statsSnapshotDistanceInMinutes);
    this.weatherManager = new WeatherManager(simulationContext.getWeatherProvider());

    this.parseFormat = simulationSettings.parserFormatterStartInfo;

    this.airplanesSimModule = new AirplanesSimModule(
        this.new SimulationModuleParent(),
        simulationSettings.trafficSettings.trafficDelayStepProbability,
        simulationSettings.trafficSettings.trafficDelayStep,
        simulationSettings.trafficSettings.useExtendedCallsigns);
    this.systemMessagesProcessor = new SystemMessagesModule(this.new SimulationModuleParent());

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
      Context.getApp().getAppLog().write(
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
    WeatherManager weatherManager = Context.getWeather().getWeatherManager();
    weatherManager.elapseSecond();
    if (weatherManager.isNewWeather()) {
      this.atcProvider.adviceWeatherUpdated();
      sendTextMessageForUser(new MetarNotification(true));
    }

    // finalize
    long elapseEndMs = System.currentTimeMillis();
    Context.getStats().getStatsProvider().registerElapseSecondDuration((int) (elapseEndMs - elapseStartMs));

    isElapseSecondCalculationRunning = false;

    // raises event
//    this.onSecondElapsed.raise();

    if (DEBUG_STYLE_TIMER)
      this.timer.start();
  }

  private void sendTextMessageForUser(IMessageContent content) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(Context.getAtc().getAtcList().getFirst(q -> q.getType() == AtcType.app)),
        content);
    MessagingAcc.getMessenger().send(m);
  }

  private void timerTicked(TimerController sender) {
    elapseSecond();
  }
}
