package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.context.AirplaneTypeAcc;
import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.context.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.atcs.context.AtcAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.ParserFormatterStartInfo;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.*;
import eng.jAtcSim.newLib.gameSim.simulation.modules.AirplanesSimModule;
import eng.jAtcSim.newLib.gameSim.simulation.modules.ISimulationModuleParent;
import eng.jAtcSim.newLib.gameSim.simulation.modules.SystemMessagesModule;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.MoodManager;
import eng.jAtcSim.newLib.mood.context.IMoodAcc;
import eng.jAtcSim.newLib.mood.context.MoodAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.speeches.system.system2user.MetarNotification;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.stats.context.IStatsAcc;
import eng.jAtcSim.newLib.stats.context.StatsAcc;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;
import eng.jAtcSim.newLib.weather.context.WeatherAcc;

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
  private final AirplanesController airplanesController = new AirplanesController();
  private final AirplanesSimModule airplanesSimModule;
  private final AirproxController airproxController;
  private final AtcProvider atcProvider;
  private final SimulationContext context;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final IOController ioController;
  private boolean isElapseSecondCalculationRunning = false;
  public ISimulation isim;
  private final MoodManager moodManager;
  private final MrvaController mrvaController;
  private final EDayTimeRun now;
  private final ParserFormatterStartInfo parseFormat;
  private final StatsProvider statsProvider;
  private final SystemMessagesModule systemMessagesProcessor;
  private final TimerController timer;
  private final TrafficProvider trafficProvider;
  private final WeatherManager weatherManager;

  public Simulation(
      SimulationContext simulationContext,
      SimulationSettings simulationSettings) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");
    ETimeStamp simulationStartTime = simulationSettings.simulationSettings.startTime;
    this.context = simulationContext;
    this.now = new EDayTimeRun(simulationStartTime.getValue());


    this.atcProvider = new AtcProvider(context.getActiveAirport());
    this.trafficProvider = new TrafficProvider(context.getTraffic());
    this.emergencyAppearanceController = new EmergencyAppearanceController(
        simulationSettings.trafficSettings.emergencyPerDayProbability);
    this.mrvaController = new MrvaController(
        context.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva));
    this.airproxController = new AirproxController();
    this.ioController = new IOController();
    this.statsProvider = new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes);
    this.weatherManager = new WeatherManager(simulationContext.getWeatherProvider());
    this.moodManager = new MoodManager();

    this.parseFormat = simulationSettings.parserFormatterStartInfo;

    this.airplanesSimModule = new AirplanesSimModule(
        this.new SimulationModuleParent(),
        simulationSettings.trafficSettings.trafficDelayStepProbability,
        simulationSettings.trafficSettings.trafficDelayStep,
        simulationSettings.trafficSettings.useExtendedCallsigns);
    this.systemMessagesProcessor = new SystemMessagesModule(this.new SimulationModuleParent());

    this.timer = new TimerController(simulationSettings.simulationSettings.secondLengthInMs, this::timerTicked);

    initializeContexts();
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

  private void initializeContexts() {
    SharedAcc sharedContext = new SharedAcc(
        this.context.getActiveAirport().getIcao(),
        this.atcProvider.getAtcIds(),
        this.now,
        new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    IAirplaneTypeAcc airplaneTypeAcc = new AirplaneTypeAcc(this.context.getAirplaneTypes());
    ContextManager.setContext(IAirplaneTypeAcc.class, airplaneTypeAcc);

    IAirplaneAcc airplaneContext = new AirplaneAcc(this.airplanesController);
    ContextManager.setContext(IAirplaneAcc.class, airplaneContext);

    IAreaAcc areaAcc = new AreaAcc(
        this.context.getArea(),
        this.context.getActiveAirport(),
        () -> this.atcProvider.getRunwayConfiguration(),
        () -> this.atcProvider.tryGetSchedulerRunwayConfiguration()
    );
    ContextManager.setContext(IAreaAcc.class, areaAcc);

    IAtcAcc atcContext = new AtcAcc(
        this.atcProvider.getAtcIds(),
        callsign -> this.atcProvider.getResponsibleAtc(callsign));
    ContextManager.setContext(IAtcAcc.class, atcContext);

    IWeatherAcc weatherContext = new WeatherAcc(this.weatherManager);
    ContextManager.setContext(IWeatherAcc.class, weatherContext);

    IStatsAcc statsContext = new StatsAcc(this.statsProvider);
    ContextManager.setContext(IStatsAcc.class, statsContext);

    IMoodAcc moodContext = new MoodAcc(this.moodManager);
    ContextManager.setContext(IMoodAcc.class, moodContext);

    //TODO Implement this: Implement this
    throw new ToDoException("Implement this");
//    IMessagingContext messagingContext = new MessagingContext( this.messenger);
//    ContextManager.setContext(IMessagingContext.class, messagingContext);
  }

  private void sendTextMessageForUser(IMessageContent content) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(Context.getAtc().getAtcList().getFirst(q -> q.getType() == AtcType.app)),
        content);
    Context.getMessaging().getMessenger().send(m);
  }

  private void timerTicked(TimerController sender) {
    elapseSecond();
  }
}
