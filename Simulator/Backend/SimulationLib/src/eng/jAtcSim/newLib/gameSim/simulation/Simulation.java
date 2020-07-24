package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.SimulationStartupContext;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.KeyShortcutManager;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.modules.*;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.MoodManager;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.weather.WeatherManager;

public class Simulation {

  private static final boolean DEBUG_STYLE_TIMER = false;
  private final AirplanesModule airplanesModule;
  private final AtcModule atcModule;
  private final IOModule ioModule;
  private boolean isElapseSecondCalculationRunning = false;
  public ISimulation isim;
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

    this.worldModule = new WorldModule(this, simulationContext);
    this.worldModule.init();

    this.atcModule = new AtcModule(new AtcProvider(worldModule.getActiveAirport()));
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

    this.statsModule = new StatsModule(this, new StatsProvider(simulationSettings.simulationSettings.statsSnapshotDistanceInMinutes));
    this.statsModule.init();

    this.ioModule = new IOModule(
        new KeyShortcutManager(),
        simulationSettings.parserFormatterStartInfo,
        new SystemMessagesModule(this)
    );
    this.ioModule.init();

    this.weatherModule = new WeatherModule(this, new WeatherManager(simulationContext.weatherProvider));
    this.weatherModule.init();

    this.timerModule = new TimerModule(simulationSettings.simulationSettings.secondLengthInMs, this::timerTicked);
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

  private void sendTextMessageForUser(IMessageContent content) {
    Message m = new Message(
        Participant.createSystem(),
        Participant.createAtc(Context.getAtc().getAtcList().getFirst(q -> q.getType() == AtcType.app)),
        content);
    Context.getMessaging().getMessenger().send(m);
  }

  private void timerTicked(TimerModule sender) {
    elapseSecond();
  }
}
