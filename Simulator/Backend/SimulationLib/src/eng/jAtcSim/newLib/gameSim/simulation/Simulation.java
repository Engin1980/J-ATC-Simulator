package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.IAirplaneList;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.gameSim.IAirplaneInfo;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.SimulationStartupContext;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.KeyShortcutManager;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.modules.*;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.mood.MoodManager;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.AtcIdList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
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
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class Simulation implements IXPersistable {

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
    public AtcIdList getAtcs() {
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
    public IReadOnlyList<AtcId> getUserAtcIds() {
      return Simulation.this.getAtcModule().getUserAtcIds();
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
      return Context.getArea().onCurrentRunwayConfigurationChange().add(() -> action.raise(Simulation.this.isim));
    }

    @Override
    public int registerOnSecondElapsed(IEventListenerSimple<ISimulation> action) {
      return Simulation.this.getTimerModule().registerOnTickListener(action);
    }

    @Override
    public void sendAtcCommand(AtcId fromAtcId, AtcId toAtcId, IAtcSpeech speech) {
      Simulation.this.getIoModule().sendAtcCommand(fromAtcId, toAtcId, speech);
    }

    @Override
    public void sendPlaneCommands(AtcId fromAtcId, Callsign toCallsign, SpeechList<IForPlaneSpeech> cmds) {
      Simulation.this.getIoModule().sendPlaneCommand(fromAtcId, toCallsign, cmds);
    }

    @Override
    public void sendSystemCommand(AtcId fromAtcId, ISystemSpeech speech) {
      Simulation.this.getIoModule().sendSystemCommand(fromAtcId, speech);
    }

    @Override
    public void sendSystemCommandAnonymous(ISystemSpeech speech) {
      Simulation.this.getIoModule().sendSystemCommandByGame(speech);
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

  //TODEL rem unecessary
  private final AirplanesModule airplanesModule;
  private final AtcModule atcModule;
  @XIgnored
  private final IOModule ioModule;
  @XIgnored
  private boolean isElapseSecondCalculationRunning = false;
  @XIgnored
  public ISimulation isim = this.new MySimulation();
  private final EDayTimeRun now;
  private final StatsModule statsModule;
  private final TimerModule timerModule;
  private final TrafficModule trafficModule;
  private final WeatherModule weatherModule;
  @XIgnored
  private final WorldModule worldModule;

  @XConstructor
  @XmlConstructor
  private Simulation(XContext ctx) {
    this.now = new EDayTimeRun(0);
    SharedAcc sharedContext = new SharedAcc(
            Context.getShared().getAirportIcao(),
            Context.getShared().getAtcs(),
            this.now,
            new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    this.airplanesModule = null;
    this.atcModule = null;
    this.ioModule = new IOModule(
            this,
            new KeyShortcutManager(),
            new SystemMessagesModule(this)
    );

    this.statsModule = null;
    this.timerModule = null;
    this.trafficModule = null;
    this.weatherModule = null;
    this.worldModule = null;
  }

  public Simulation(
          SimulationStartupContext simulationContext,
          SimulationSettings simulationSettings) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");

    ETimeStamp simulationStartTime = simulationSettings.simulationSettings.startTime;
    this.now = new EDayTimeRun(simulationStartTime.getValue());

    this.worldModule = new WorldModule(this, simulationContext);

    this.weatherModule = new WeatherModule(this, new WeatherManager(simulationContext.weatherProvider));

    this.ioModule = new IOModule(
            this,
            new KeyShortcutManager(),
            new SystemMessagesModule(this)
    );

    this.statsModule = new StatsModule(this, new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes));

    this.atcModule = new AtcModule(
            new AtcProvider(worldModule.getActiveAirport()));

    this.trafficModule = new TrafficModule(
            this,
            new TrafficProvider(simulationContext.traffic),
            simulationSettings.trafficSettings.trafficDelayStepProbability,
            simulationSettings.trafficSettings.trafficDelayStep,
            simulationSettings.trafficSettings.useExtendedCallsigns);

    this.airplanesModule = new AirplanesModule(
            this,
            new AirplanesController(),
            new AirproxController(),
            new MrvaController(worldModule.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva)),
            new EmergencyAppearanceController(simulationSettings.trafficSettings.emergencyPerDayProbability),
            new MoodManager()
    );

    this.timerModule = new TimerModule(this, simulationSettings.simulationSettings.secondLengthInMs);
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

  public void init() {
    SharedAcc sharedContext = new SharedAcc(
            this.worldModule.getActiveAirport().getIcao(),
            this.worldModule.getActiveAirport().getAtcTemplates().select(q -> q.toAtcId()),
            this.now,
            new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    this.worldModule.init();
    this.weatherModule.init();
    this.ioModule.init();
    this.statsModule.init();
    this.atcModule.init();
    this.trafficModule.init();
    this.airplanesModule.init();

    this.timerModule.registerOnTickListener(this::timerTicked);
  }

  @Override
  public void load(XElement elm, XContext ctx) {
    ctx.loader.parents.set(this);

    ctx.loader.loadField(this, "airplanesModule", elm);
    IAirplaneList lst = new IAirplaneList();
    lst.addMany(this.airplanesModule.getPlanes());
    ctx.loader.values.set(lst);
  }

  public void reinitAfterLoad() {
    SharedAcc sharedContext = new SharedAcc(
            Context.getShared().getAirportIcao(),
            Context.getShared().getAtcs(),
            this.now,
            Context.getShared().getSimLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    ctx.saver.saveRemainingFields(this, elm);
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
