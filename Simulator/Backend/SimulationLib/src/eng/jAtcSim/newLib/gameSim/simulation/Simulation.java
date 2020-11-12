package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneXmlContextInit;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
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
import eng.jAtcSim.newLib.mood.MoodXmlContextInit;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.CallsignFactory;
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
import eng.jAtcSim.newLib.weather.XmlWeatherContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectSerializer;

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
      return Simulation.this.getAtcModule().getOnRunwayChanged().add(() -> action.raise(Simulation.this.isim));
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

  public static void prepareXmlContext(XmlContext ctx) {

    ctx.sdfManager.setSerializer(Simulation.class, new ObjectSerializer()
            .withValueClassCheck(Simulation.class, false)
            .withIgnoredFields(
                    "ioModule", // nothing to save
                    "isim", // accessor, not to save
                    "worldModule" // not saved
            ));

    // region AirplanesModule
    ctx.sdfManager.setSerializer(AirplanesModule.class, new ObjectSerializer()
            .withValueClassCheck(AirplanesModule.class)
            .withIgnoredFields("planes4public", "planesPrepared")
            .withIgnoredField("parent"));

    ctx.sdfManager.setSerializer(AirproxController.class, new ObjectSerializer());
    ctx.sdfManager.setSerializer(EmergencyAppearanceController.class, new ObjectSerializer());
    ctx.sdfManager.setSerializer(MrvaController.class,
            new ObjectSerializer()
                    .withIgnoredFields("mrvas", "mrvaMaps"));

    MoodXmlContextInit.prepareXmlContext(ctx);
    AirplaneXmlContextInit.prepareXmlContext(ctx);
    // endregion

    // region AtcModule
    ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer()
            .withIgnoredField("userAtcsCache")
            .withIgnoredFields("parent"));
    AtcProvider.prepareXmlContext(ctx);
    // endregion

    // region StatsModule
    ctx.sdfManager.setSerializer(StatsModule.class, new ObjectSerializer()
            .withIgnoredField("parent"));
    StatsProvider.prepareXmlContext(ctx);
    // endregion

    // region TimerModule
    ctx.sdfManager.setSerializer(TimerModule.class, new ObjectSerializer()
            .withCustomFieldFormatter("tmr", q -> q == null ? "false" : "true")
            .withIgnoredFields("tickEvent", "parent"));
    // endregion

    // region TrafficModule
    ctx.sdfManager.setSerializer(TrafficModule.class, new ObjectSerializer()
            .withIgnoredField("parent"));
    ctx.sdfManager.setSerializer(CallsignFactory.class, new ObjectSerializer());
    ctx.sdfManager.setSerializer(TrafficProvider.class, new ObjectSerializer()
            .withIgnoredField("trafficModel"));
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.traffic.movementTemplating");
    // endregion

    // region WeatherModule
    ctx.sdfManager.setSerializer(WeatherModule.class, new ObjectSerializer()
            .withIgnoredField("parent"));
    XmlWeatherContextInit.prepareXmlContext(ctx);
    // endregion

    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.messaging");

//TODEL
//    XElement tmp;
//
//    XmlSaveUtils.Field.storeField(target, this, "airplanesModule",
//            (XElement e, AirplanesModule q) -> q.save(e));
//
//    XmlSaveUtils.Field.storeField(target, this, "atcModule",
//            (XElement e, AtcModule q) -> q.save(e));
//
//    XmlSaveUtils.Field.storeField(target, this, "timerModule",
//            (XElement e, TimerModule q) -> q.save(e));
//
//    XmlSaveUtils.Field.storeField(target, this, "trafficModule",
//            (XElement e, TrafficModule q) -> q.save(e));
//
//    XmlSaveUtils.Field.storeField(target, this, "weatherModule",
//            (XElement e, WeatherModule q) -> q.save(e));
//
//    // ioModule not saved, no need
//
//    XmlSaveUtils.Field.storeField(target, this, "statsModule",
//            (XElement e, StatsModule q) -> q.save(e));

    // worldModule not saved, no need
  }

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

  public Simulation(SimulationStartupContext simulationContext, XElement source) {
    //TODEL
    throw new ToDoException();
//    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
//    EAssert.Argument.isNotNull(source, "source");
//
//    now = new EDayTimeRun(0);
//    XmlLoadUtils.Field.restoreField(source, this, "now", SharedXmlUtils.Parsers.dayTimeRunParser);
//
//    SharedAcc sharedContext = new SharedAcc(
//            simulationContext.activeAirport.getIcao(),
//            simulationContext.activeAirport.getAtcTemplates().select(q -> q.toAtcId()),
//            this.now,
//            new SimulationLog()
//    );
//    ContextManager.setContext(ISharedAcc.class, sharedContext);
//
//    // world module not saved, so not loaded
//    this.worldModule = new WorldModule(this, simulationContext);
//    this.worldModule.init();
//
//    // io module not saved, not loaded
//    this.ioModule = new IOModule(
//            this,
//            new KeyShortcutManager(),
//            new SystemMessagesModule(this)
//    );
//    this.ioModule.init();
//
//    // here the loading starts:
//
//    this.weatherModule = XmlLoadUtils.Field.loadFieldValue(source, "weatherModule",
//            e -> WeatherModule.load(this, simulationContext.weatherProvider, source));
//    //this.weatherModule.init(); - i guess not necessary
//    //TODO
//    throw new ToDoException("Continue here");
////    this.airplanesModule = XmlLoadUtils.Field.loadFieldValue(source, "airplanesModule",
////            e -> AirplanesModule.load(this, e, null));
////
////    XmlLoadUtils.Field.restoreField(source, this, "trafficModule",
////            (Deserializer) e -> TrafficModule.load(this, simulationContext.traffic, e));
////
////    // tady odsud nové přepisování
////    // this should be the last in the queue as it may start the timer
////    this.timerModule = XmlLoadUtils.Field.loadFieldValue(source, "timerModule", e -> TimerModule.load(this, e));
////    this.timerModule.registerOnTickListener(this::timerTicked);
//
//    /*
//    must be loaded:
//    airplanesModule
//    atcModule
//    trafficModule (done)
//    weatherModule (done)
//    statsModule
//     */
//
////    this.statsModule = new StatsModule(this, new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes));
////    this.statsModule.init();
////
////    this.atcModule = new AtcModule(
////            new AtcProvider(worldModule.getActiveAirport()));
////    this.atcModule.init();
////
////
////
////    this.airplanesModule = new AirplanesModule(
////            this,
////            new AirplanesController(),
////            new AirproxController(),
////            new MrvaController(worldModule.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva)),
////            new EmergencyAppearanceController(simulationSettings.trafficSettings.emergencyPerDayProbability),
////            new MoodManager()
////    );
////    this.airplanesModule.init();
////
////

  }

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

    this.worldModule = new WorldModule(this, simulationContext);
    this.worldModule.init();

    this.weatherModule = new WeatherModule(this, new WeatherManager(simulationContext.weatherProvider));
    this.weatherModule.init();

    this.ioModule = new IOModule(
            this,
            new KeyShortcutManager(),
            new SystemMessagesModule(this)
    );
    this.ioModule.init();

    this.statsModule = new StatsModule(this, new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes));
    this.statsModule.init();

    this.atcModule = new AtcModule(
            new AtcProvider(worldModule.getActiveAirport()));
    this.atcModule.init();

    this.trafficModule = new TrafficModule(
            this,
            new TrafficProvider(simulationContext.traffic),
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

  public void save(XElement target) {

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
