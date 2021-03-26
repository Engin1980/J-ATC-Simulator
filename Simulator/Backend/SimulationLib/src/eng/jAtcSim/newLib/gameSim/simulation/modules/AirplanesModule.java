package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.context.AirplaneAcc;
import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.gameSim.IAirplaneInfo;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.IScheduledMovement;
import eng.jAtcSim.newLib.gameSim.simulation.ScheduledMovement;
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
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.FinishedPlaneStats;

import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AirplanesModule extends SimulationModule {

  private final AirplanesController airplanesController;
  private final AirproxController airproxController;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MoodManager moodManager;
  private final MrvaController mrvaController;
  private final IList<IAirplaneInfo> planes4public = new EList<>();
  private final IList<AirplaneTemplate> planesPrepared = new EList<>();

  @XConstructor

  private AirplanesModule() {
    super((Simulation) null);
    this.airplanesController = null;
    this.airproxController = null;
    this.emergencyAppearanceController = null;
    this.moodManager = null;
    this.mrvaController = null;
  }

  @XConstructor
  private AirplanesModule(XLoadContext ctx, AirplanesController airplanesController, AirproxController airproxController, EmergencyAppearanceController emergencyAppearanceController, MoodManager moodManager, MrvaController mrvaController) {
    super(ctx);
    this.airplanesController = airplanesController;
    this.airproxController = airproxController;
    this.emergencyAppearanceController = emergencyAppearanceController;
    this.moodManager = moodManager;
    this.mrvaController = mrvaController;
  }

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
    this.planesPrepared.addMany(newTemplates);
  }

  public void deletePlane(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    Callsign callsign = this.airplanesController.getPlanes().getFirst(q -> q.getSqwk().equals(squawk)).getCallsign();
    this.removePlane(callsign, true);
    this.moodManager.unregisterCallsign(callsign);
  }

  public void elapseSecond() {
    insertNewPlanes();
    removeOldPlanes();
    generateEmergencyIfRequired();
    updatePlanes();
    evaluateFails();
  }

  public AirproxType getAirproxForPlane(IAirplane airplane) {
    return this.airproxController.getAirproxForPlane(airplane);
  }

  public IReadOnlyList<IAirplane> getPlanes() {
    return this.airplanesController.getPlanes();
  }

  public IReadOnlyList<IAirplaneInfo> getPlanesForPublicAccess() {
    return this.planes4public;
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

    //TODEL
    //FIXME
    if (parent.getAtcModule() == null){
      System.out.println("Pauza - proč je to null???");
    }

    planesAtHoldingPoint = parent.getAtcModule().getPlanesCountAtHoldingPoint();

    AnalysedPlanes ret = new AnalysedPlanes(arrivals, departures, appArrivals, appDepartures, mrvaErrors, airproxErrors, planesAtHoldingPoint);
    return ret;
  }

  public IReadOnlyList<IScheduledMovement> getScheduledMovements() {
    //TODO do in some better way let not the object is always created for every time
    IReadOnlyList<IScheduledMovement> ret = this.planesPrepared.select(q -> new ScheduledMovement(q));
    return ret;
  }

  public void init() {
    IAirplaneAcc airplaneContext = new AirplaneAcc(this.airplanesController);
    ContextManager.setContext(IAirplaneAcc.class, airplaneContext);

    IMoodAcc moodContext = new MoodAcc(this.moodManager);
    ContextManager.setContext(IMoodAcc.class, moodContext);

    this.airplanesController.init();

    this.airplanesController.getPlanes().forEach(q -> this.planes4public.add(new AirplaneInfo(q, this.parent)));
  }

  public boolean isMrvaErrorForPlane(IAirplane airplane) {
    return this.mrvaController.isMrvaErrorForPlane(airplane);
  }

  public void removePlane(Callsign callsign, boolean isForced) {
    super.parent.getAtcModule().unregisterPlane(callsign, isForced);
    this.planes4public.remove(q -> q.callsign().equals(callsign));
    this.moodManager.unregisterCallsign(callsign);
    this.airplanesController.unregisterPlane(callsign);
    Context.getMessaging().getMessenger().unregisterListener(Participant.createAirplane(callsign));
  }

  @Override
  public void save(XElement elm, XSaveContext ctx) {
    super.save(elm, ctx);
    ctx.fields.ignoreFields(this,
            "planes4public",
            "planesPrepared");

    //TODEL not required
    // ctx.saveRemainingFields(this, elm);
  }

  @Override
  public void load(XElement elm, XLoadContext ctx) {
    super.load(elm, ctx);

    ctx.fields.ignoreFields(this,
            "planes4public",
            "planesPrepared");
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

  private void evaluateFails() {
    this.mrvaController.evaluateMrvaFails(this.getPlanes());
    this.mrvaController.getMrvaViolatingPlanes()
            .where(q -> Context.getAtc().getResponsibleAtcId(q) != null && Context.getAtc().getResponsibleAtcId(q).getType() == AtcType.app)
            .forEach(q -> Context.getMood().getMoodManager().get(q).experience(Mood.SharedExperience.mrvaViolation));

    this.airproxController.evaluateAirproxFails(this.getPlanes());
    this.airproxController.getAirproxViolatingPlanes()
            .where(q -> (Context.getAtc().getResponsibleAtcId(q.getKey()) == null || Context.getAtc().getResponsibleAtcId(q.getKey()).getType() == AtcType.app)
                    && q.getValue() == AirproxType.full)
            .forEach(q -> Context.getMood().getMoodManager().get(q.getKey()).experience(Mood.SharedExperience.airprox));
  }

  private Squawk generateAvailableSquawk() {
    IList<Squawk> squawks = this.airplanesController.getPlanes().select(q -> q.getSqwk());
    Squawk ret;
    do {
      ret = Squawk.generate();
      if (squawks.contains(ret)) ret = null;
    } while (ret == null);
    return ret;
  }

  private void generateEmergencyIfRequired() {
    if (emergencyAppearanceController.isEmergencyTimeElapsed(Context.getShared().getNow())) {
      if (!Context.getAirplane().getAirplanes().isAny(q -> q.isEmergency())) {
        airplanesController.throwEmergency();
      }
      emergencyAppearanceController.generateEmergencyTime(Context.getShared().getNow());
    }
  }

  private void insertNewPlanes() {
    int index = 0;
    while (index < planesPrepared.count()) {
      AirplaneTemplate at = planesPrepared.get(index);
      if (at instanceof ArrivalAirplaneTemplate && isInSeparationConflictWithTraffic((ArrivalAirplaneTemplate) at))
        index++;
      else {
        IAirplane tmp = this.airplanesController.registerPlane(at, generateAvailableSquawk());
        Context.getMessaging().getMessenger().registerListener(Participant.createAirplane(tmp.getCallsign()));
        this.moodManager.registerCallsign(tmp.getCallsign());
        this.planes4public.add(new AirplaneInfo(tmp, this.parent));
        super.parent.getAtcModule().registerNewPlane(tmp);


        planesPrepared.removeAt(index);
      }
    }
  }

  private boolean isInSeparationConflictWithTraffic(ArrivalAirplaneTemplate template) {
    Integer checkedAtEntryPointSeconds = null;

    boolean ret = false;

    for (IAirplane rdr : this.airplanesController.getPlanes()) {
      if (rdr.isDeparture())
        continue;
      if (rdr.getAtc().getTunedAtc().getType() != AtcType.ctr)
        continue;

      if (template.getEntryPoint().getNavaid().equals(rdr.getRouting().getEntryExitPoint()) == false)
        continue;

      double dist = Coordinates.getDistanceInNM(
              rdr.getRouting().getEntryExitPoint().getCoordinate(), rdr.getCoordinate());
      int atEntryPointSeconds = (int) (dist / rdr.getSha().getSpeed() * 3600);

      if (checkedAtEntryPointSeconds == null) {
        dist = Coordinates.getDistanceInNM(
                template.getEntryPoint().getNavaid().getCoordinate(), template.getCoordinate());
        checkedAtEntryPointSeconds = (int) (dist / template.getSpeed() * 3600);
      }

      if (Math.abs(atEntryPointSeconds - checkedAtEntryPointSeconds) < 120) {
        ret = true;
        break;
      }
    }
    return ret;
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
              && Context.getAtc().getResponsibleAtcId(p.getCallsign()) != null
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
      this.removePlane(plane.getCallsign(), false);
    }
  }

  private void updatePlanes() {
    airplanesController.updatePlanes();
  }
}

class AirplaneInfo implements IAirplaneInfo {

  private final IAirplane airplane;
  private final Simulation sim;

  public AirplaneInfo(IAirplane airplane, Simulation sim) {
    this.sim = sim;
    this.airplane = airplane;
  }


  @Override
  public int altitude() {
    return airplane.getSha().getAltitude();
  }

  @Override
  public Callsign callsign() {
    return airplane.getCallsign();
  }

  @Override
  public Coordinate coordinate() {
    return airplane.getCoordinate();
  }

  @Override
  public Navaid entryExitPoint() {
    return airplane.getRouting().getEntryExitPoint();
  }

  @Override
  public AirproxType getAirprox() {
    return this.sim.getAirplanesModule().getAirproxForPlane(airplane);
  }

  @Override
  public DepartureArrival getArriDep() {
    return airplane.isDeparture() ? DepartureArrival.departure : DepartureArrival.arrival;
  }

  @Override
  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return airplane.getRouting().getAssignedRunwayThreshold();
  }

  @Override
  public String getRoutingLabel() {
    String ret;
    if (airplane.getRouting().getAssignedDARouteName() == null)
      ret = sf("(%s)",
              airplane.getRouting().getEntryExitPoint().getName()
      );
    else
      ret = sf("%s/%s",
              airplane.getRouting().getAssignedRunwayThreshold().getName(),
              airplane.getRouting().getAssignedDARouteName()
      );
    return ret;
  }

  @Override
  public boolean hasRadarContact() {
    return airplane.getAtc().hasRadarContact();
  }

  @Override
  public int heading() {
    return airplane.getSha().getHeading();
  }

  @Override
  public int ias() {
    return airplane.getSha().getSpeed();
  }

  @Override
  public boolean isDeparture() {
    return airplane.isDeparture();
  }

  @Override
  public boolean isEmergency() {
    return airplane.isEmergency();
  }

  @Override
  public boolean isMrvaError() {
    return this.sim.getAirplanesModule().isMrvaErrorForPlane(this.airplane);
  }

  @Override
  public boolean isUnderConfirmedSwitch() {
    return false;
  }

  @Override
  public AirplaneType planeType() {
    return airplane.getType();
  }

  @Override
  public AtcId responsibleAtc() {
    return this.sim.getAtcModule().getResponsibleAtc(airplane);
  }

  @Override
  public Squawk squawk() {
    return airplane.getSqwk();
  }

  @Override
  public String status() {
    return airplane.getState().toString();
  }

  @Override
  public int targetAltitude() {
    return airplane.getSha().getTargetAltitude();
  }

  @Override
  public int targetHeading() {
    return airplane.getSha().getTargetHeading();
  }

  @Override
  public int targetSpeed() {
    //TODO remove this, probably useless?
    return airplane.getSha().getTargetSpeed();
  }

  @Override
  public double tas() {
    double m = 1 + this.altitude() / 100000d;
    double ret = this.ias() * m;
    return ret;
  }

  @Override
  public AtcId tunedAtc() {
    return airplane.getAtc().getTunedAtc();
  }

  @Override
  public int verticalSpeed() {
    return airplane.getSha().getVerticalSpeed();
  }
}
