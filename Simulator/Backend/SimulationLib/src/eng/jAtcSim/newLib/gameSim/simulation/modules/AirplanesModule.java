package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.context.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.IScheduledMovement;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimulationModule;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.mood.MoodManager;
import eng.jAtcSim.newLib.mood.context.IMoodAcc;
import eng.jAtcSim.newLib.mood.context.MoodAcc;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.FinishedPlaneStats;

public class AirplanesModule extends SimulationModule {
  private final AirproxController airproxController;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final AirplanesController airplanesController;
  private final MoodManager moodManager;

  public AirplanesModule(Simulation parent, AirplanesController airplanesController, AirproxController airproxController, MrvaController mrvaController, EmergencyAppearanceController emergencyAppearanceController, MoodManager moodManager) {
    super(parent);
    EAssert.Argument.isNotNull(airplanesController, "airplanesController");
    EAssert.Argument.isNotNull(airproxController, "airproxController");
    EAssert.Argument.isNotNull(mrvaController, "mrvaController");
    EAssert.Argument.isNotNull(emergencyAppearanceController, "emergencyAppearanceController");
    EAssert.Argument.isNotNull(moodManager, "moodManager");

    this.airproxController = airproxController;
    this.emergencyAppearanceController = emergencyAppearanceController;
    this.mrvaController = mrvaController;
    this.airplanesController = airplanesController;
    this.moodManager = moodManager;
  }

  public void addNewPreparedPlanes(IList<AirplaneTemplate> newTemplates) {
    this.airplanesController.addNewPreparedPlanes(newTemplates);
  }

  public void deletePlane(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    Callsign callsign = this.airplanesController.getPlanes().getFirst(q->q.getSqwk().equals(squawk)).getCallsign();
    this.removePlane(callsign);
  }

  public void elapseSecond() {
    //insertNewPlanes();
    removeOldPlanes();
    generateEmergencyIfRequired();
    updatePlanes();
    evaluateFails();
  }

  public IReadOnlyList<IAirplane> getPlanes() {
    return this.airplanesController.getPlanes();
  }

  public AnalysedPlanes getPlanesForStats() {
    IReadOnlyList<IAirplane> planes = airplanesController.getPlanes();
    int arrivals = 0;
    int departures = 0;
    int appArrivals = 0;
    int appDepartures = 0;
    int mrvaErrors;
    int airproxErrors;
    int planesAtHoldingPoint;

    for (IAirplane plane : planes) {
      if (plane.isArrival())
        arrivals++;
      else
        departures++;
      if (plane.getAtc().getTunedAtc().getType() == AtcType.app) {
        if (plane.isArrival())
          appArrivals++;
        else
          appDepartures++;
      }
    }
    mrvaErrors = this.mrvaController.getMrvaViolatingPlanes().count();
    airproxErrors = this.airproxController.getAirproxViolatingPlanes().count();
    planesAtHoldingPoint = parent.getAtcModule().getPlanesCountAtHoldingPoint();

    AnalysedPlanes ret = new AnalysedPlanes(arrivals, departures, appArrivals, appDepartures, mrvaErrors, airproxErrors, planesAtHoldingPoint);
    return ret;
  }

  public IReadOnlyList<IScheduledMovement> getScheduledMovements() {
    ContextManager.getContext(IAppAcc.class).getAppLog().write(ApplicationLog.eType.info,
        "TODO: AirplaneModules.getScheduledMovements not implemented.");
    return new EList<>();
  }

  public void init() {
    IAirplaneAcc airplaneContext = new AirplaneAcc(this.airplanesController);
    ContextManager.setContext(IAirplaneAcc.class, airplaneContext);

    IMoodAcc moodContext = new MoodAcc(this.moodManager);
    ContextManager.setContext(IMoodAcc.class, moodContext);
  }

  private void removeOldPlanes() {
    IList<IAirplane> ret = new EList<>();

    for (IAirplane p : Context.getAirplane().getAirplanes()) {
      // landed
      if (p.isArrival() && p.getSha().getSpeed() < 11) {
        ret.add(p);
        Context.getStats().getStatsProvider().registerFinishedPlane(buildFinishedAirplaneStats(p));
      }

      // departed
      if (p.isDeparture()
          && Context.getAtc().getResponsibleAtcId(p.getCallsign()).getType() == AtcType.ctr
          && Coordinates.getDistanceInNM(
          p.getCoordinate(),
          Context.getArea().getAirport().getLocation()) > Context.getArea().getAirport().getCoveredDistance()) {
        ret.add(p);
        Context.getStats().getStatsProvider().registerFinishedPlane(buildFinishedAirplaneStats(p));
      }

      if (p.isEmergency() && p.hasElapsedEmergencyTime()) {
        ret.add(p);
      }
    }

    for (IAirplane plane : ret) {
      this.removePlane(plane.getCallsign());
    }
  }

  public void removePlane(Callsign callsign){
    this.airplanesController.unregisterPlane(callsign);
    Context.getMessaging().getMessenger().unregisterListener(Participant.createAirplane(callsign));
    this.mrvaController.unregisterPlane(callsign);
  }

  private void updatePlanes() {
    airplanesController.elapseSecond();
  }

  private void generateEmergencyIfRequired() {
    if (emergencyAppearanceController.isEmergencyTimeElapsed(Context.getShared().getNow())) {
      if (!Context.getAirplane().getAirplanes().isAny(q -> q.isEmergency())) {
        airplanesController.throwEmergency();
      }
      emergencyAppearanceController.generateEmergencyTime(Context.getShared().getNow());
    }
  }

  private void evaluateFails() {
    this.mrvaController.evaluateMrvaFails();
    this.mrvaController.getMrvaViolatingPlanes()
        .where(q -> Context.getAtc().getResponsibleAtcId(q).getType() == AtcType.app)
        .forEach(q -> Context.getMood().getMoodManager().get(q).experience(Mood.SharedExperience.mrvaViolation));

    this.airproxController.evaluateAirproxFails();
    this.airproxController.getAirproxViolatingPlanes()
        .where(q -> Context.getAtc().getResponsibleAtcId(q.getKey()).getType() == AtcType.app
            && q.getValue() == AirproxType.full)
        .forEach(q -> Context.getMood().getMoodManager().get(q.getKey()).experience(Mood.SharedExperience.airprox));
  }

  private FinishedPlaneStats buildFinishedAirplaneStats(IAirplane airplane) {
    int entryDelay = airplane.getFlight().getEntryDelay();
    int exitDelay = airplane.getFlight().getExitDelay();
    int delayDifference = exitDelay - entryDelay;
    FinishedPlaneStats ret = new FinishedPlaneStats(
        airplane.getCallsign(), airplane.isDeparture(), airplane.isEmergency(), delayDifference,
        Context.getMood().getMoodManager().getMoodResult(airplane.getCallsign(), delayDifference)
    );
    return ret;
  }
}
