package eng.jAtcSim.newLib.atcs.internal.tower;


import eng.eSystem.collections.*;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.ComputerAtc;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedForTakeoffCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc2atc.StringResponse;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherAcc;

public class TowerAtc extends ComputerAtc {
  public static class RunwayCheck {
    private static final int[] RWY_CHECK_ANNOUNCE_INTERVALS = new int[]{30, 15, 10, 5};

    private static final int MIN_NORMAL_MAINTENANCE_INTERVAL = 200;
    private static final int MAX_NORMAL_MAINTENANCE_INTERVAL = 240;
    private static final int NORMAL_MAINTENACE_DURATION = 5;
    private static final int MIN_SNOW_MAINTENANCE_INTERVAL = 45;
    private static final int MAX_SNOW_MAINTENANCE_INTERVAL = 180;
    private static final int MIN_SNOW_INTENSIVE_MAINTENANCE_INTERVAL = 20;
    private static final int MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL = 45;
    private static final int SNOW_MAINENANCE_DURATION = 20;
    private static final int MIN_EMERGENCY_MAINTENANCE_DURATION = 5;
    private static final int MAX_EMERGENCY_MAINTENANCE_DURATION = 45;

    public static RunwayCheck createImmediateAfterEmergency() {
      int closeDuration = SharedAcc.getRnd().nextInt(MIN_EMERGENCY_MAINTENANCE_DURATION, MAX_EMERGENCY_MAINTENANCE_DURATION);
      RunwayCheck ret = new RunwayCheck(0, closeDuration);
      return ret;
    }

    public static RunwayCheck createNormal(boolean isInitial) {
      int maxTime;
      if (isInitial)
        maxTime = SharedAcc.getRnd().nextInt(MAX_NORMAL_MAINTENANCE_INTERVAL);
      else
        maxTime = SharedAcc.getRnd().nextInt(MIN_NORMAL_MAINTENANCE_INTERVAL, MAX_NORMAL_MAINTENANCE_INTERVAL);

      RunwayCheck ret = new RunwayCheck(maxTime, NORMAL_MAINTENACE_DURATION);
      return ret;
    }

    public static RunwayCheck createSnowCleaning(boolean isInitial, boolean isIntensive) {
      int maxTime = isIntensive
          ? SharedAcc.getRnd().nextInt(MIN_SNOW_INTENSIVE_MAINTENANCE_INTERVAL, MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL)
          : SharedAcc.getRnd().nextInt(MIN_SNOW_MAINTENANCE_INTERVAL, MAX_SNOW_MAINTENANCE_INTERVAL);
      if (isInitial)
        maxTime = SharedAcc.getRnd().nextInt(maxTime);

      RunwayCheck ret = new RunwayCheck(maxTime, SNOW_MAINENANCE_DURATION);
      return ret;
    }
    private int expectedDurationInMinutes;
    private EDayTimeStamp realDurationEnd;
    private SchedulerForAdvice scheduler;

    private RunwayCheck(int minutesToNextCheck, int expectedDurationInMinutes) {
      EDayTimeStamp et = SharedAcc.getNow().toStamp().addMinutes(minutesToNextCheck);
      this.scheduler = new SchedulerForAdvice(et, RWY_CHECK_ANNOUNCE_INTERVALS);
      this.expectedDurationInMinutes = expectedDurationInMinutes;
    }

    public boolean isActive() {
      return scheduler == null;
    }

    public void start() {
      double durationRangeSeconds = expectedDurationInMinutes * 60 * 0.2d;
      int realDurationSeconds = (int) SharedAcc.getRnd().nextDouble(
          expectedDurationInMinutes * 60 - durationRangeSeconds,
          expectedDurationInMinutes * 60 + durationRangeSeconds);
      realDurationEnd = SharedAcc.getNow().toStamp().addSeconds(realDurationSeconds);
      this.scheduler = null;
    }
  }

  public static class RunwaysInUseInfo {
    private SchedulerForAdvice scheduler;
    private RunwayConfiguration current;
    private RunwayConfiguration scheduled;

    public RunwayConfiguration getCurrent() {
      return current;
    }

    public RunwayConfiguration getScheduled() {
      return scheduled;
    }
  }

  public enum eDirection {
    departures,
    arrivals
  }

  private static final int[] RWY_CHANGE_ANNOUNCE_INTERVALS = new int[]{30, 15, 10, 5, 4, 3, 2, 1};
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;

  private static RunwayConfiguration getSuggestedThresholds() {
    RunwayConfiguration ret = null;
    Weather w = WeatherAcc.getWeather();

    for (RunwayConfiguration rc : AreaAcc.getAirport().getRunwayConfigurations()) {
      if (rc.accepts(w.getWindHeading(), w.getWindSpeetInKts())) {
        ret = rc;
        break;
      }
    }
    if (ret == null) {
      ActiveRunwayThreshold rt = getSuggestedThresholdsRegardlessRunwayConfigurations();
      IReadOnlyList<ActiveRunwayThreshold> rts = rt.getParallelGroup();
      ret = RunwayConfiguration.createForThresholds(rts);
    }

    assert ret != null : "There must be runway configuration created.";

    return ret;
  }

  private static ActiveRunwayThreshold getSuggestedThresholdsRegardlessRunwayConfigurations() {
    Weather w = WeatherAcc.getWeather();
    Airport airport = AreaAcc.getAirport();
    ActiveRunwayThreshold rt = null;

    double diff = Integer.MAX_VALUE;
    // select runway according to wind
    for (ActiveRunway r : airport.getRunways()) {
      for (ActiveRunwayThreshold t : r.getThresholds()) {
        double localDiff = Headings.getDifference(w.getWindHeading(), (int) t.getCourse(), true);
        if (localDiff < diff) {
          diff = localDiff;
          rt = t;
        }
      }
    }
    return rt;
  }

  private final DepartureManager departureManager = new DepartureManager(this);
  private final ArrivalManager arrivalManager = new ArrivalManager(this);
  private final EventAnonymousSimple onRunwayChanged = new EventAnonymousSimple();
  private RunwaysInUseInfo inUseInfo = null;
  private EMap<ActiveRunway, RunwayCheck> runwayChecks = null;
  private boolean isUpdatedWeather;

  public TowerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  @Override
  public void elapseSecond() {
    super.elapseSecond();

    //TODO here should be some check that landing plane is not landing on the occupied runway
    tryTakeOffPlaneNew();
    processRunwayCheckBackground();
    processRunwayChangeBackground();
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return departureManager.getNumberOfPlanesAtHoldingPoint();
  }

  public EventAnonymousSimple getOnRunwayChanged() {
    return onRunwayChanged;
  }

  public RunwayConfiguration getRunwayConfigurationInUse() {
    return inUseInfo.current;
  }

  @Override
  public void init() {
    super.init();

    runwayChecks = new EMap<>();
    for (ActiveRunway runway : AreaAcc.getAirport().getRunways()) {
      RunwayCheck rc = RunwayCheck.createNormal(true);
      runwayChecks.set(runway, rc);
    }

    inUseInfo = new RunwaysInUseInfo();
    inUseInfo.current = getSuggestedThresholds();
    inUseInfo.scheduler = null;
  }

  public boolean isRunwayThresholdUnderMaintenance(ActiveRunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent()).isActive() == false;
    return ret;
  }

  @Override
  public void registerNewPlaneUnderControl(IAirplane plane, boolean initialRegistration) {
    if (plane.isArrival())
      arrivalManager.registerNewArrival(plane);
    else {
      ActiveRunwayThreshold runwayThreshold = getRunwayThresholdForDeparture(plane);
      departureManager.registerNewDeparture(plane, runwayThreshold);
    }
  }

  @Override
  public void removePlaneDeletedFromGame(IAirplane plane) {
    if (plane.isArrival()) {
      arrivalManager.deletePlane(plane);
      //TODO this will add to stats even planes deleted from the game by a user(?)
      //TODO this stats value should be increased outside of Tower atc??
      Acc.stats().registerArrival();
    }
    if (plane.isDeparture()) {
      departureManager.deletePlane(plane);
    }
  }

  public void setUpdatedWeatherFlag() {
    this.isUpdatedWeather = true;
  }

  public RunwayConfiguration tryGetRunwayConfigurationScheduled() {
    return inUseInfo.scheduled;
  }

  @Override
  public void unregisterPlaneUnderControl(IAirplane plane) {
    if (plane.isArrival()) {
      if (plane.getState() == AirplaneState.landed) {
        arrivalManager.unregisterFinishedArrival(plane);
      }
      //GO-AROUNDed planes are not unregistered, they have been unregistered previously
    }
    if (plane.isDeparture()) {
      departureManager.unregisterFinishedDeparture(plane);

      // add to stats
      EDayTimeStamp holdingPointEntryTime = departureManager.getAndEraseHoldingPointEntryTime(plane);
      int diffSecs = SharedAcc.getNow().getValue() - holdingPointEntryTime.getValue();
      diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
      if (diffSecs < 0) diffSecs = 0;
      Acc.stats().registerDeparture(diffSecs);
    }

    if (plane.isEmergency() && plane.getState() == AirplaneState.landed) {
      // if it is landed emergency, close runway for amount of time
      ActiveRunway rwy = plane.getRouting().getAssignedRunwayThreshold().getParent();
      RunwayCheck rwyCheck = RunwayCheck.createImmediateAfterEmergency();
      runwayChecks.set(rwy, rwyCheck);
    }
  }

//  @Override
//  protected void _load(XElement elm) {
//    super._load(elm);
//    LoadSave.loadField(elm, this, "departureManager");
//    LoadSave.saveField(elm, this, "arrivalManager");
//    LoadSave.loadField(elm, this, "inUseInfo");
//    LoadSave.loadField(elm, this, "runwayChecks");
//    LoadSave.loadField(elm, this, "isUpdatedWeather");
//  }
//
//  @Override
//  protected void _save(XElement elm) {
//    super._save(elm);
//    LoadSave.saveField(elm, this, "departureManager");
//    LoadSave.saveField(elm, this, "arrivalManager");
//    LoadSave.saveField(elm, this, "inUseInfo");
//    LoadSave.saveField(elm, this, "runwayChecks");
//    LoadSave.saveField(elm, this, "isUpdatedWeather");
//  }

  @Override
  protected boolean acceptsNewRouting(IAirplane plane, SwitchRoutingRequest srr) {
    assert plane.isDeparture() : "It is nonsense to have this call here for arrival.";

    boolean ret;
    RunwayConfiguration rc;
    if (inUseInfo.getScheduled() != null && inUseInfo.scheduler.getSecondsLeft() < 300) {
      rc = inUseInfo.getScheduled();
    } else {
      rc = inUseInfo.getCurrent();
    }

//    ret = rc.getArrivals()
//        .isNone(q -> q.getThreshold() == srr.threshold);
//
//    if (ret) {
    ret = srr.threshold.getRoutes()
        .isAny(q -> (q == srr.route || srr.route.getType() == DARouteType.vectoring)
            && srr.route.isValidForCategory(plane.getType().category)
            && srr.route.getMaxMrvaAltitude() <= plane.getType().maxAltitude
            && q.getMainNavaid().equals(plane.getRouting().getEntryExitPoint()));
//    }
    return ret;
  }

  private void announceChangeRunwayInUse() {
    String msgTxt = this.inUseInfo.scheduled.toInfoString("\n");
    msgTxt = "Expected runway change to:\n" + msgTxt +
        "\nExpected runway change at " + this.inUseInfo.scheduler.getScheduledTime().toTimeString();
    sendMessageToUser(msgTxt);
  }

  private void announceScheduledRunwayCheck(ActiveRunway rwy, RunwayCheck rc) {
    StringResponse cnt;
    if (rc.isActive())
      cnt = StringResponse.create("Runway %s is under maintenance right now until approximately %s.",
          rwy.getName(),
          rc.realDurationEnd.toHourMinuteString()
      );
    else {
      cnt = StringResponse.create("Runway %s maintenance is scheduled at %s for approximately %d minutes.",
          rwy.getName(),
          rc.scheduler.getScheduledTime().toHourMinuteString(),
          rc.expectedDurationInMinutes);
      rc.scheduler.nowAnnounced();
    }

    Message msg = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(msg);

  }

  private void beginRunwayMaintenance(ActiveRunway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s is now in progress for approx %d minutes.", runway.getName(), rc.expectedDurationInMinutes);
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc.start();
  }

  @Override
  protected ComputerAtc.RequestResult canIAcceptPlane(IAirplane p) {
    if (p.getFlightModule().isDeparture()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is a departure.", p.getFlightModule().getCallsign()));
    }
    if (getPrm().getResponsibleAtc(p) != Acc.atcApp()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is not from APP.", p.getFlightModule().getCallsign()));
    }
    if (isOnApproachOfTheRunwayInUse(p) == false)
      return new ComputerAtc.RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", p.getFlightModule().getCallsign()));
    if (isRunwayThresholdUnderMaintenance(p.getRoutingModule().getAssignedRunwayThreshold()) == false) {
      return new RequestResult(false, String.format("Runway %s is closed now.", p.getRoutingModule().getAssignedRunwayThreshold().getParent().getName()));
    }
    if (p.getSha().getAltitude() > this.acceptAltitude) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too high.", p.getFlightModule().getCallsign()));
    }
    double dist = Coordinates.getDistanceInNM(p.getCoordinate(), AreaAcc.getAirport().getLocation());
    if (dist > MAXIMAL_ACCEPT_DISTANCE_IN_NM) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too far.", p.getFlightModule().getCallsign()));
    }

    return new RequestResult(true, null);
  }

  private void changeRunwayInUse() {

    String msgTxt = "Changed runway(s) in use now to: " + inUseInfo.scheduled.toInfoString("; ");
    sendMessageToUser(msgTxt);

    this.inUseInfo.current = this.inUseInfo.scheduled;
    this.inUseInfo.scheduled = null;
    this.inUseInfo.scheduler = null;

    IList<ActiveRunway> tmp =
        this.inUseInfo.current.getDepartures()
            .select(q -> q.getThreshold().getParent())
            .union(this.inUseInfo.current.getArrivals().select(q -> q.getThreshold().getParent()));
    tmp.forEach(q -> announceScheduledRunwayCheck(q, this.runwayChecks.get(q)));

    onRunwayChanged.raise();
  }

  private void checkForRunwayChange() {
    RunwayConfiguration newSuggested = getSuggestedThresholds();

    boolean isSame = inUseInfo.current.isUsingTheSameRunwayConfiguration(newSuggested);
    if (!isSame) {
      inUseInfo.scheduler = new SchedulerForAdvice(Acc.now().addSeconds(10 * 60), RWY_CHANGE_ANNOUNCE_INTERVALS);
      inUseInfo.scheduled = newSuggested;
    }
  }

  private void finishRunwayMaintenance(ActiveRunway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s has ended.", runway.getName()
    );
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc = RunwayCheck.createNormal(false);
    runwayChecks.set(runway, rc);
  }

  private double getDepartingPlaneSwitchAltitude(char category) {
    switch (category) {
      case 'A':
        return (double) AreaAcc.getAirport().getAltitude() + Acc.rnd().nextInt(100, 250);
      case 'B':
        return (double) AreaAcc.getAirport().getAltitude() + Acc.rnd().nextInt(150, 400);
      case 'C':
      case 'D':
        return (double) AreaAcc.getAirport().getAltitude() + Acc.rnd().nextInt(200, 750);
      default:
        throw new EEnumValueUnsupportedException(category);
    }
  }

  private ActiveRunwayThreshold getRunwayThresholdForDeparture(IAirplane plane) {
    ActiveRunwayThreshold ret;
    IList<ActiveRunwayThreshold> rts = inUseInfo.current.getDepartures()
        .where(q -> q.isForCategory(plane.getType().category))
        .select(q -> q.getThreshold());
    assert rts.size() > 0 : "No runway for airplane kind " + plane.getType().name;
    ret = rts.getRandom();
    restrictToRunwaysNotUnderLongMaintenance(rts, true);
    if (rts.size() > 0)
      ret = rts.getRandom();
    return ret;
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(IAirplane plane) {
    Atc ret = null;
    if (this.arrivalManager.checkIfPlaneIsReadyToSwitchAndRemoveIt(plane)) {
      ret = Acc.atcApp();
    } else if (plane.getFlightModule().isDeparture()) {
      ret = Acc.atcApp();
    }
    return ret;
  }

  private boolean isOnApproachOfTheRunwayInUse(IAirplane p) {
    boolean ret = p.getEmergencyModule().isEmergency() || inUseInfo.current.getArrivals()
        .isAny(q -> q.getThreshold().equals(p.getRoutingModule().getAssignedRunwayThreshold()) && q.isForCategory(p.getType().category));
    return ret;
  }

  private boolean isRunwayCrossSetUnderActiveApproach(ActiveRunway runway) {
    boolean ret;
    ActiveRunwayThreshold rt = runway.getThresholdA();
    ISet<ActiveRunwayThreshold> crts = inUseInfo.current.getCrossedSetForThreshold(rt);
    double dist = crts.min(q -> arrivalManager.getClosestLandingPlaneDistanceForThreshold(q), 100d);
    if (dist < 2.5)
      ret = true;
    else
      ret = false;
    return ret;
  }

  private boolean isRunwayCrossSetUsedByRolling(ActiveRunway runway) {
    boolean ret;
    ActiveRunwayThreshold rt = runway.getThresholdA();
    ISet<ActiveRunwayThreshold> crts = inUseInfo.current.getCrossedSetForThreshold(rt);
    ret = crts
        .isAny(q ->
            arrivalManager.isSomeArrivalOnRunway(rt.getParent())
                || departureManager.isSomeDepartureOnRunway(rt.getParent()));
    return ret;
  }

  private boolean isRunwayOccupiedDueToDeparture(ActiveRunwayThreshold runwayThreshold) {
    boolean ret = false;
    ActiveRunwayThreshold rt = runwayThreshold;

    int clearAltitude = runwayThreshold.getParent().getParent().getAltitude() + 1500;
    ISet<ActiveRunwayThreshold> crts = inUseInfo.current.getCrossedSetForThreshold(rt);

    for (ActiveRunwayThreshold crt : crts) {
      IAirplane4Atc lastDep = departureManager.tryGetTheLastDepartedPlane(crt);
      if (lastDep == null) continue; // no last departure
      if (lastDep.getSha().getAltitude() > clearAltitude) continue; // last departure has safe altitude
      double dist = Coordinates.getDistanceInNM(rt.getOtherThreshold().getCoordinate(), lastDep.getCoordinate());
      if (dist > 5) continue; // last departure has safe distance
      //boolean hasSafeSeparation = Separation.isSafeSeparation(lastDep, toPlane, (int) rt.getCourse(), 120);
      //if (hasSafeSeparation) continue; // has safe separation

      ret = true;
      break;
    }
    return ret;
  }

  private boolean isRunwayThresholdHavingRecentDeparture(ActiveRunwayThreshold runwayThreshold) {
    boolean ret = departureManager.getLastDepartureTime(runwayThreshold).addSeconds(60).isAfterOrEq(Acc.now());
    return ret;
  }

  private boolean isRunwayUnderMaintenance(ActiveRunway runway) {
    return this.runwayChecks.get(runway).isActive();
  }

  private void processMessageFromAtc(RunwayUse ru) {

    if (ru.isAsksForChange()) {
      if (inUseInfo.scheduled == null) {
        Message msg = new Message(
            this,
            Acc.atcApp(),
            new StringMessageContent("There is no scheduled runway change."));
        super.sendMessage(msg);
      } else {
        // force runway change
        changeRunwayInUse();
      }
    } else {
      String msgTxt = inUseInfo.current.toInfoString("\n");
      sendMessageToUser("Runway(s) in use: \n" + msgTxt);

      if (inUseInfo.scheduled != null) {
        msgTxt = inUseInfo.scheduled.toInfoString("\n");
        msgTxt = "Scheduled runways to use: \n" + msgTxt +
            ". \nScheduled runways active from " + inUseInfo.scheduler.getScheduledTime().toHourMinuteString();
        sendMessageToUser(msgTxt);
      }
    }
  }

  private void processMessageFromAtc(eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.RunwayCheck rrct) {
    if (rrct.type == eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.RunwayCheck.eType.askForTime) {
      RunwayCheck rc = this.runwayChecks.tryGet(rrct.runway);
      if (rc != null)
        announceScheduledRunwayCheck(rrct.runway, rc);
      else {
        for (ActiveRunway runway : this.runwayChecks.getKeys()) {
          rc = this.runwayChecks.get(runway);
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    } else if (rrct.type == eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.RunwayCheck.eType.doCheck) {
      ActiveRunway rwy = rrct.runway;
      RunwayCheck rc = this.runwayChecks.tryGet(rwy);
      if (rwy == null && this.runwayChecks.size() == 1) {
        rwy = this.runwayChecks.getKeys().getFirst();
        rc = this.runwayChecks.get(rwy);
      }
      if (rc == null) {
        Message msg = new Message(this, Acc.atcApp(),
            StringResponse.createRejection("Sorry, you must specify exact runway (threshold) at which I can start the maintenance."));
        super.sendMessage(msg);
      } else {
        if (rc.isActive()) {
          Message msg = new Message(this, Acc.atcApp(),
              StringResponse.createRejection("The runway %s is already under maintenance right now.",
                  rwy.getName()));
          super.sendMessage(msg);
        } else if (rc.scheduler.getMinutesLeft() > 30) {
          Message msg = new Message(this, Acc.atcApp(),
              StringResponse.createRejection("Sorry, the runway %s is scheduled for the maintenance in more than 30 minutes.",
                  rwy.getName()));
          super.sendMessage(msg);
        } else {
          Message msg = new Message(this, Acc.atcApp(),
              StringResponse.create("The maintenance of the runway %s is approved and will start shortly.",
                  rwy.getName()));
          super.sendMessage(msg);
          rc.scheduler.setApprovedTrue();
        }
      }
    }
  }

  @Override
  protected void processMessagesFromPlane(IAirplane plane, SpeechList spchs) {
    if (spchs.containsType(GoingAroundNotification.class)) {
      arrivalManager.goAroundPlane(plane);
    }
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    if (m.getContent() instanceof eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.RunwayCheck) {
      eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.RunwayCheck rrct = m.getContent();
      processMessageFromAtc(rrct);
    } else if (m.getContent() instanceof RunwayUse) {
      RunwayUse ru = m.getContent();
      processMessageFromAtc(ru);
    }
  }

  private void processRunwayChangeBackground() {
    if (inUseInfo.scheduler == null) {
      if (isUpdatedWeather) {
        if (Acc.weather().getSnowState() != Weather.eSnowState.none)
          updateRunwayMaintenanceDueToSnow();
        checkForRunwayChange();
        isUpdatedWeather = false;
      }
    } else {
      if (inUseInfo.scheduler.isElapsed()) {
        changeRunwayInUse();
      } else if (inUseInfo.scheduler.shouldBeAnnouncedNow()) {
        announceChangeRunwayInUse();
      }
    }
  }

  private void processRunwayCheckBackground() {
    for (ActiveRunway runway : runwayChecks.getKeys()) {
      RunwayCheck rc = runwayChecks.get(runway);
      if (rc.isActive()) {
        if (rc.realDurationEnd.isBeforeOrEq(Acc.now()))
          finishRunwayMaintenance(runway, rc);
      } else {
        if (rc.scheduler.isElapsed()) {
          if (this.departureManager.isSomeDepartureOnRunway(runway) == false && this.arrivalManager.isSomeArrivalApproachingOrOnRunway(runway))
            beginRunwayMaintenance(runway, rc);
        } else if (rc.scheduler.shouldBeAnnouncedNow()) {
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    }
  }

  private void restrictToRunwaysNotUnderLongMaintenance(IList<ActiveRunwayThreshold> rts, boolean onlyLongTimeMaintenance) {
    rts.remove(q -> {
      ActiveRunway r = q.getParent();
      RunwayCheck rt = runwayChecks.get(r);
      boolean ret = rt.isActive() && rt.expectedDurationInMinutes > 5;
      return ret;
    });
  }

  private void sendMessageToUser(String text) {
    Message msg = new Message(this, Acc.atcApp(),
        new StringMessageContent(text));
    super.sendMessage(msg);
  }

  @Override
  protected boolean shouldBeSwitched(IAirplane plane) {
    if (plane.isArrival())
      return true; // this should be go-arounded arrivals

    // as this plane is asked for switch, it is confirmed
    // from APP, so can be moved from holding-point to line-up
    departureManager.confirmedByApproach(plane);

    return departureManager.canBeSwitched(plane);
  }

  private void tryTakeOffPlaneNew() {

    // checks for lined-up plane
    IMap<ActiveRunwayThreshold, IAirplane> tmp = departureManager.getTheLinedUpPlanes();
    if (tmp.isEmpty()) return; // no-one is ready to departure
    IAirplane toReadyPlane = null;
    for (ActiveRunwayThreshold runwayThreshold : tmp.getKeys()) {
      ActiveRunway runway = runwayThreshold.getParent();
      if (isRunwayUnderMaintenance(runway)) continue;
      if (isRunwayCrossSetUnderActiveApproach(runway)) continue;
      if (isRunwayCrossSetUsedByRolling(runway)) continue;
      if (isRunwayThresholdHavingRecentDeparture(runwayThreshold)) continue;
      if (isRunwayOccupiedDueToDeparture(runwayThreshold)) continue;
      toReadyPlane = tmp.get(runwayThreshold);
      break;
    }

    if (toReadyPlane == null) return; // no-one has runway for deprature

    ActiveRunwayThreshold availableThreshold = toReadyPlane.getRouting().getAssignedRunwayThreshold();

    // if it gets here, the "toReadyPlane" can proceed take-off
    // add to stats
    departureManager.departAndGetHoldingPointEntryTime(toReadyPlane, availableThreshold, getDepartingPlaneSwitchAltitude(toReadyPlane.getType().category));
    toReadyPlane.setHoldingPointState(availableThreshold);

    SpeechList lst = new SpeechList();
    lst.add(new RadarContactConfirmationNotification());

    // TO altitude only when no altitude from SID already processed
    if (toReadyPlane.getSha().getTargetAltitude() <= availableThreshold.getParent().getParent().getAltitude())
      lst.add(ChangeAltitudeCommand.create(
          ChangeAltitudeCommand.eDirection.climb, availableThreshold.getInitialDepartureAltitude()));

    lst.add(new ClearedForTakeoffCommand(availableThreshold.getName()));

    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAirplane(toReadyPlane.getCallsign()),
        lst);
    super.sendMessage(m);
  }

  private void updateRunwayMaintenanceDueToSnow() {
    for (ActiveRunway key : this.runwayChecks.getKeys()) {
      RunwayCheck rc = this.runwayChecks.get(key);
      if (rc.isActive()) continue;
      int maxInterval = WeatherAcc.getWeather().getSnowState() == Weather.eSnowState.intensive
          ? RunwayCheck.MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL
          : RunwayCheck.MAX_SNOW_MAINTENANCE_INTERVAL;
      if (rc.scheduler.getMinutesLeft() > maxInterval) {
        rc = RunwayCheck.createSnowCleaning(
            false,
            WeatherAcc.getWeather().getSnowState() == Weather.eSnowState.intensive);
        runwayChecks.set(key, rc);
        announceScheduledRunwayCheck(key, rc);
      }
    }
  }

}