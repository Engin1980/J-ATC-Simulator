package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.gameSim.IAirplaneInfo;
import eng.jAtcSim.newLib.gameSim.Message;
import eng.jAtcSim.newLib.gameSim.IParseFormat;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.SimulationStartupContext;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.KeyShortcutManager;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.modules.*;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.mood.MoodManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;
import eng.jAtcSim.newLib.stats.IStatsProvider;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class Simulation {

  public class MySimulation implements ISimulation {

    @Override
    public Airport getAirport() {
      return Simulation.this.getWorldModule().getActiveAirport();
    }

    @Override
    public ApplicationLog getAppLog() {
      return ContextManager.getContext(IAppAcc.class).getAppLog();
    }

    @Override
    public Area getArea() {
      return Simulation.this.getWorldModule().getArea();
    }

    @Override
    public AtcList<AtcId> getAtcs() {
      return Simulation.this.getAtcModule().getAtcs();
    }

    @Override
    public IList<Message> getMessages(Object key) {
      return Simulation.this.getIoModule().getMessagesByKey(key);
    }

    @Override
    public EDayTimeStamp getNow() {
      return Simulation.this.now.toStamp();
    }

    @Override
    public IParseFormat getParseFormat() {
      return null;
    }

    @Override
    public IReadOnlyList<IAirplaneInfo> getPlanesToDisplay() {
      return Simulation.this.getAirplanesModule().getPlanesForPublicAccess();
    }

    @Override
    public RunwayConfiguration getRunwayConfigurationInUse() {
      return Simulation.this.getAtcModule().getRunwayConfiguration();
    }

    @Override
    public IReadOnlyList<IScheduledMovement> getScheduledMovements() {
      return Simulation.this.getAirplanesModule().getScheduledMovements();
    }

    @Override
    public IStatsProvider getStats() {
      return Simulation.this.getStatsModule().getStatsProvider();
    }

    @Override
    public AtcId getUserAtcId() {
      return Simulation.this.getAtcModule().getUserAtcId();
    }

    @Override
    public void pauseUnpauseSim() {
      if (Simulation.this.getTimerModule().isRunning())
        this.stop();
      else
        this.start();
    }

    @Override
    public void registerMessageListener(Object listener, Messenger.ListenerAim... aims) {
      Simulation.this.getIoModule().registerMessageListener(listener, aims);
    }

    @Override
    public int registerOnRunwayChanged(IEventListenerSimple<ISimulation> action) {
      return Simulation.this.getAtcModule().getOnRunwayChanged().add(() -> action.raise(Simulation.this.isim));
    }

    @Override
    public int registerOnSecondElapsed(IEventListenerSimple<ISimulation> action) {
      return Simulation.this.getTimerModule().registerOnTickListener(action);
    }

    @Override
    public void sendAtcCommand(AtcId id, IAtcSpeech speech) {
      Simulation.this.getIoModule().sendAtcCommand(id, speech);
    }

    @Override
    public void sendPlaneCommands(Callsign callsign, SpeechList<IForPlaneSpeech> cmds) {
      Simulation.this.getIoModule().sendPlaneCommand(callsign, cmds);
    }

    @Override
    public void sendSystemCommand(ISystemSpeech speech) {
      Simulation.this.getIoModule().sendSystemCommand(speech);
    }

    @Override
    public void start() {
      Simulation.this.getTimerModule().start();
    }

    @Override
    public void stop() {
      Simulation.this.getTimerModule().stop();
    }

    @Override
    public void unregisterMessageListener(Object listener) {
      Simulation.this.getIoModule().registerMessageListener(listener);
    }

    @Override
    public void unregisterOnSecondElapsed(int simulationSecondListenerHandlerId) {
      Simulation.this.getTimerModule().unregisterOnTickListener(simulationSecondListenerHandlerId);
    }
  }

  private static final boolean DEBUG_STYLE_TIMER = false;
  private final AirplanesModule airplanesModule;
  private final AtcModule atcModule;
  private final IOModule ioModule;
  private boolean isElapseSecondCalculationRunning = false;
  public ISimulation isim = this.new MySimulation();
  private final EDayTimeRun now;
  private final StatsModule statsModule;
  private final TimerModule timerModule;
  private final TrafficModule trafficModule;
  private final WeatherModule weatherModule;
  private final WorldModule worldModule;

  public Simulation(
      SimulationStartupContext simulationContext,
      SimulationSettings simulationSettings) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");

    ETimeStamp simulationStartTime = simulationSettings.simulationSettings.startTime;
    this.now = new EDayTimeRun(simulationStartTime.getValue());
    SharedAcc sharedContext = new SharedAcc(
        simulationContext.activeAirport.getIcao(),
        simulationContext.activeAirport.getAtcTemplates().select(q -> q.toAtcId()),
        this.now,
        new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    AtcId userAtcId = simulationContext.activeAirport.getAtcTemplates().select(q -> q.toAtcId()).getFirst(q -> q.getType() == AtcType.app);

    this.worldModule = new WorldModule(this, simulationContext);
    this.worldModule.init();

    this.weatherModule = new WeatherModule(this, new WeatherManager(simulationContext.weatherProvider));
    this.weatherModule.init();

    this.ioModule = new IOModule(
        this,
        userAtcId,
        new KeyShortcutManager(),
        simulationSettings.parserFormatterStartInfo,
        new SystemMessagesModule(this, userAtcId)
    );
    this.ioModule.init();

    this.statsModule = new StatsModule(this, new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes));
    this.statsModule.init();

    this.atcModule = new AtcModule(
        userAtcId,
        new AtcProvider(worldModule.getActiveAirport()));
    this.atcModule.init();

    this.trafficModule = new TrafficModule(
        this,
        new TrafficProvider(worldModule.getTraffic()),
        simulationSettings.trafficSettings.trafficDelayStepProbability,
        simulationSettings.trafficSettings.trafficDelayStep,
        simulationSettings.trafficSettings.useExtendedCallsigns);
    this.trafficModule.init();

    this.airplanesModule = new AirplanesModule(
        this,
        new AirplanesController(),
        new AirproxController(),
        new MrvaController(worldModule.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva)),
        new EmergencyAppearanceController(simulationSettings.trafficSettings.emergencyPerDayProbability),
        new MoodManager()
    );
    this.airplanesModule.init();


    this.timerModule = new TimerModule(this, simulationSettings.simulationSettings.secondLengthInMs);
    this.timerModule.registerOnTickListener(this::timerTicked);
  }

  public AirplanesModule getAirplanesModule() {
    return airplanesModule;
  }

  public AtcModule getAtcModule() {
    return atcModule;
  }

  public IOModule getIoModule() {
    return ioModule;
  }

  public StatsModule getStatsModule() {
    return statsModule;
  }

  public TimerModule getTimerModule() {
    return timerModule;
  }

  public TrafficModule getTrafficModule() {
    return trafficModule;
  }

  public WeatherModule getWeatherModule() {
    return weatherModule;
  }

  public WorldModule getWorldModule() {
    return worldModule;
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
      this.timerModule.stop();
    isElapseSecondCalculationRunning = true;
    now.increaseSecond();

    // process system messages
    this.ioModule.elapseSecond();

    // traffic stuff
    this.trafficModule.elapseSecond();

    // airplanes stuff
    this.airplanesModule.elapseSecond();

    // atc stuff
    this.atcModule.elapseSecond();

    // stats here
    this.statsModule.elapseSecond();

    // weather
    this.weatherModule.elapseSecond();

    // finalize
    this.statsModule.elapseSecond();
    long elapseEndMs = System.currentTimeMillis();
    this.statsModule.registerElapseSecondDuration((int) (elapseEndMs - elapseStartMs));

    isElapseSecondCalculationRunning = false;

    // raises event
//    this.onSecondElapsed.raise();

    if (DEBUG_STYLE_TIMER)
      this.timerModule.start();
  }

  private void timerTicked(ISimulation sender) {
    elapseSecond();
  }
}
