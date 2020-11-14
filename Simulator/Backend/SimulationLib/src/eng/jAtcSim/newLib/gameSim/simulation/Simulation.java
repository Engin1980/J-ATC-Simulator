package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.IEventListenerSimple;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.AtcList;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.atcs.AtcXmlContextInit;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
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
import eng.jAtcSim.newLib.stats.StatsXmlContextInit;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;
import eng.jAtcSim.newLib.weather.WeatherXmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.annotations.XmlConstructor;
import eng.newXmlUtils.implementations.ObjectDeserializer;
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
    // region Simulation
    ctx.sdfManager.setSerializer(Simulation.class, new ObjectSerializer()
            .withValueClassCheck(Simulation.class, false)
            .withIgnoredFields(
                    "ioModule", // nothing to save
                    "isim", // accessor, not to save
                    "worldModule" // not saved
            ));
    ctx.sdfManager.setDeserializer(Simulation.class, new ObjectDeserializer<Simulation>()
            .withIgnoredFields("ioModule", "isim", "worldModule")
            .withAfterLoadAction((q, c) -> {
              WorldModule worldModule = new WorldModule(
                      q,
                      (Area) c.values.get("area"),
                      (Airport) c.values.get("airport"),
                      (AirplaneTypes) c.values.get("airplaneTypes"),
                      (AirlinesFleets) c.values.get("companyFleets"),
                      (GeneralAviationFleets) c.values.get("gaFleets"));
              ReflectionUtils.FieldUtils.set(q, "worldModule", worldModule);
              c.values.set("simulation", q);
            }));

    // endregion

    // region AirplanesModule
    ctx.sdfManager.setSerializer(AirplanesModule.class, new ObjectSerializer()
            .withValueClassCheck(AirplanesModule.class)
            .withIgnoredFields("planes4public")
            .withIgnoredFields("parent"));

    ctx.sdfManager.setDeserializer(AirplanesModule.class, new ObjectDeserializer<AirplanesModule>()
            .withIgnoredFields("planes4public")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation"))
            .withAfterLoadAction((q, c) -> q.init()));

    ctx.sdfManager.setSerializer(AirproxController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(AirproxController.class, new ObjectDeserializer<AirproxController>());

    ctx.sdfManager.setSerializer(EmergencyAppearanceController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(EmergencyAppearanceController.class, new ObjectDeserializer<EmergencyAppearanceController>());

    ctx.sdfManager.setSerializer(MrvaController.class,
            new ObjectSerializer()
                    .withIgnoredFields("mrvas", "mrvaMaps"));
    ctx.sdfManager.setDeserializer(MrvaController.class, new ObjectDeserializer<MrvaController>()
            .withInstanceFactory(c -> new MrvaController(((Area) c.values.get("area")).getBorders().where(q -> q.getType() == Border.eType.mrva)))
            .withIgnoredFields("mrvas", "mrvaMaps"));

    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.airplanes.templates");
    // endregion

    // region AtcModule
    ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer()
            .withIgnoredFields("userAtcsCache")
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(AtcModule.class, new ObjectDeserializer<AtcModule>()
            .withIgnoredFields("userAtcsCache")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    AtcXmlContextInit.prepareXmlContext(ctx);

    // endregion

    // region StatsModule
    ctx.sdfManager.setSerializer(StatsModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(StatsModule.class, new ObjectDeserializer<StatsModule>()
            .withIgnoredFields("parent")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    StatsXmlContextInit.prepareXmlContext(ctx);
    // endregion

    // region TimerModule
    ctx.sdfManager.setSerializer(TimerModule.class, (XElement e, Object v, XmlContext c) -> {
      TimerModule t = (TimerModule) v;
      if (t.isRunning())
        e.setContent("y" + t.getTickInterval());
      else
        e.setContent("n" + t.getTickInterval());
    });
    ctx.sdfManager.setDeserializer(TimerModule.class, (e, c) -> {
      Simulation sim = (Simulation) c.values.get("simulation");
      int tickInterval = Integer.parseInt(e.getContent().substring(1));
      TimerModule ret = new TimerModule(sim, tickInterval);
      if (e.getContent().charAt(0) == 'y')
        ret.start();
      return ret;
    });
    // endregion

    // region TrafficModule
    ctx.sdfManager.setSerializer(TrafficModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(TrafficModule.class, new ObjectDeserializer<TrafficModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));
    // endregion

    // region WeatherModule
    ctx.sdfManager.setSerializer(WeatherModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(WeatherModule.class, new ObjectDeserializer<WeatherModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    WeatherXmlContextInit.prepareXmlContext(ctx);
    // endregion
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

  @XmlConstructor
  private Simulation() {
    this.airplanesModule = null;
    this.atcModule = null;
    this.ioModule = new IOModule(
            this,
            new KeyShortcutManager(),
            new SystemMessagesModule(this)
    );
    this.ioModule.init();
    this.now = null;
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
