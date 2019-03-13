package eng.jAtcSim.lib.atcs;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.SchedulerForAdvice;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.StringResponse;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.ActiveRunway;
import eng.jAtcSim.lib.world.RunwayConfiguration;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

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

    private int expectedDurationInMinutes;
    private ETime realDurationEnd;
    private SchedulerForAdvice scheduler;

    public static RunwayCheck createNormal(boolean isInitial) {
      int maxTime;
      if (isInitial)
        maxTime = Acc.rnd().nextInt(MAX_NORMAL_MAINTENANCE_INTERVAL);
      else
        maxTime = Acc.rnd().nextInt(MIN_NORMAL_MAINTENANCE_INTERVAL, MAX_NORMAL_MAINTENANCE_INTERVAL);

      RunwayCheck ret = new RunwayCheck(maxTime, NORMAL_MAINTENACE_DURATION);
      return ret;
    }

    public static RunwayCheck createSnowCleaning(boolean isInitial, boolean isIntensive) {
      int maxTime = isIntensive
          ? Acc.rnd().nextInt(MIN_SNOW_INTENSIVE_MAINTENANCE_INTERVAL, MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL)
          : Acc.rnd().nextInt(MIN_SNOW_MAINTENANCE_INTERVAL, MAX_SNOW_MAINTENANCE_INTERVAL);
      if (isInitial)
        maxTime = Acc.rnd().nextInt(maxTime);

      RunwayCheck ret = new RunwayCheck(maxTime, SNOW_MAINENANCE_DURATION);
      return ret;
    }

    public static RunwayCheck createImmediateAfterEmergency() {
      int closeDuration = Acc.rnd().nextInt(MIN_EMERGENCY_MAINTENANCE_DURATION, MAX_EMERGENCY_MAINTENANCE_DURATION);
      RunwayCheck ret = new RunwayCheck(0, closeDuration);
      return ret;
    }

    @XmlConstructor
    private RunwayCheck() {
    }

    private RunwayCheck(int minutesToNextCheck, int expectedDurationInMinutes) {
      ETime et = Acc.now().addMinutes(minutesToNextCheck);
      this.scheduler = new SchedulerForAdvice(et, RWY_CHECK_ANNOUNCE_INTERVALS);
      this.expectedDurationInMinutes = expectedDurationInMinutes;
    }

    public boolean isActive() {
      return scheduler == null;
    }

    public void start() {
      double durationRangeSeconds = expectedDurationInMinutes * 60 * 0.2d;
      int realDurationSeconds = (int) Acc.rnd().nextDouble(
          expectedDurationInMinutes * 60 - durationRangeSeconds,
          expectedDurationInMinutes * 60 + durationRangeSeconds);
      realDurationEnd = Acc.now().addSeconds(realDurationSeconds);
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
  private final DepartureManager departureManager = new DepartureManager();
  private final ArrivalManager arrivalManager = new ArrivalManager();
  @XmlIgnore
  private final EventAnonymousSimple onRunwayChanged = new EventAnonymousSimple();
  private RunwaysInUseInfo inUseInfo = null;
  private EMap<ActiveRunway, RunwayCheck> runwayChecks = null;
  private boolean isUpdatedWeather;

  private static RunwayConfiguration getSuggestedThresholds() {
    RunwayConfiguration ret = null;
    Weather w = Acc.weather();

    for (RunwayConfiguration rc : Acc.airport().getRunwayConfigurations()) {
      if (rc.accepts(w.getWindHeading(), w.getWindSpeetInKts())) {
        ret = rc;
        break;
      }
    }
    if (ret == null) {
      ActiveRunwayThreshold rt = getSuggestedThresholdsRegardlessRunwayConfigurations();
      IList<ActiveRunwayThreshold> rts = rt.getParallelGroup();
      ret = RunwayConfiguration.createForThresholds(rts);
    }

    assert ret != null : "There must be runway configuration created.";

    return ret;
  }

  private void updateRunwayMaintenanceDueToSnow() {
    for (ActiveRunway key : this.runwayChecks.getKeys()) {
      RunwayCheck rc = this.runwayChecks.get(key);
      if (rc.isActive()) continue;
      int maxInterval = Acc.weather().getSnowState() == Weather.eSnowState.intensive
          ? RunwayCheck.MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL
          : RunwayCheck.MAX_SNOW_MAINTENANCE_INTERVAL;
      if (rc.scheduler.getMinutesLeft() > maxInterval)
      {
        rc = TowerAtc.RunwayCheck.createSnowCleaning(false, Acc.weather().getSnowState() == Weather.eSnowState.intensive);
        runwayChecks.set(key, rc);
        announceScheduledRunwayCheck(key, rc);
      }
    }
  }

  private static ActiveRunwayThreshold getSuggestedThresholdsRegardlessRunwayConfigurations() {
    Weather w = Acc.weather();
    ActiveRunwayThreshold rt = null;
    if (w.getWindSpeetInKts() <= MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY) {
      for (ActiveRunway r : Acc.airport().getRunways()) {
        for (ActiveRunwayThreshold t : r.getThresholds()) {
          if (t.isPreferred()) {
            rt = t;
            break;
          }
        }
        if (rt != null) {
          break;
        }
      }
    }

    double diff = Integer.MAX_VALUE;
    if (rt == null) {
      // select runway according to wind
      for (ActiveRunway r : Acc.airport().getRunways()) {
        for (ActiveRunwayThreshold t : r.getThresholds()) {
          double localDiff = Headings.getDifference(w.getWindHeading(), (int) t.getCourse(), true);
          if (localDiff < diff) {
            diff = localDiff;
            rt = t;
          }
        }
      }
    }
    return rt;
  }

  public TowerAtc(AtcTemplate template) {
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

  @Override
  protected boolean acceptsNewRouting(Airplane plane, SwitchRoutingRequest srr) {
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
        .isAny(q -> (q == srr.route || srr.route.getType() == Route.eType.vectoring)
            && srr.route.isValidForCategory(plane.getType().category)
            && srr.route.getMaxMrvaAltitude() <= plane.getType().maxAltitude
            && q.getMainNavaid().equals(plane.getEntryExitFix()));
//    }
    return ret;
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    if (m.getContent() instanceof eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck) {
      eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck rrct = m.getContent();
      processMessageFromAtc(rrct);
    } else if (m.getContent() instanceof RunwayUse) {
      RunwayUse ru = m.getContent();
      processMessageFromAtc(ru);
    }
  }

  @Override
  protected boolean shouldBeSwitched(Airplane plane) {
    if (plane.isArrival())
      return true; // this should be go-arounded arrivals

    // as this plane is asked for switch, it is confirmed
    // from APP, so can be moved from holding-point to line-up
    departureManager.confirmedByApproach(plane);

    if (departureManager.canBeSwitched(plane))
      return true;

    return false;
  }

  @Override
  protected ComputerAtc.RequestResult canIAcceptPlane(Airplane p) {
    if (p.isDeparture()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is a departure.", p.getCallsign()));
    }
    if (getPrm().getResponsibleAtc(p) != Acc.atcApp()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is not from APP.", p.getCallsign()));
    }
    if (isOnApproachOfTheRunwayInUse(p) == false)
      return new ComputerAtc.RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", p.getCallsign()));
    if (isRunwayThresholdUnderMaintenance(p.tryGetCurrentApproachRunwayThreshold()) == false) {
      return new RequestResult(false, String.format("Runway %s is closed now.", p.tryGetCurrentApproachRunwayThreshold().getParent().getName()));
    }
    if (p.getAltitude() > this.acceptAltitude) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too high.", p.getCallsign()));
    }
    double dist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
    if (dist > MAXIMAL_ACCEPT_DISTANCE_IN_NM) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too far.", p.getCallsign()));
    }

    return new RequestResult(true, null);
  }

  @Override
  protected void processMessagesFromPlane(Airplane plane, SpeechList spchs) {
    if (spchs.containsType(GoingAroundNotification.class)) {
      arrivalManager.goAroundPlane(plane);
    }
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(Airplane plane) {
    Atc ret = null;
    if (this.arrivalManager.checkIfPlaneIsReadyToSwitchAndRemoveIt(plane)) {
      ret = Acc.atcApp();
    } else if (plane.isDeparture()) {
      ret = Acc.atcApp();
    }
    return ret;
  }

  @Override
  protected void _save(XElement elm) {
    super._save(elm);
    LoadSave.saveField(elm, this, "departureManager");
    LoadSave.saveField(elm, this, "arrivalManager");
    LoadSave.saveField(elm, this, "inUseInfo");
    LoadSave.saveField(elm, this, "runwayChecks");
    LoadSave.saveField(elm, this, "isUpdatedWeather");
  }

  @Override
  protected void _load(XElement elm) {
    super._load(elm);
    LoadSave.loadField(elm, this, "departureManager");
    LoadSave.saveField(elm, this, "arrivalManager");
    LoadSave.loadField(elm, this, "inUseInfo");
    LoadSave.loadField(elm, this, "runwayChecks");
    LoadSave.loadField(elm, this, "isUpdatedWeather");
  }

  @Override
  public void init() {
    super.init();

    runwayChecks = new EMap<>();
    for (ActiveRunway runway : Acc.airport().getRunways()) {
      RunwayCheck rc = TowerAtc.RunwayCheck.createNormal(true);
      runwayChecks.set(runway, rc);
    }

    inUseInfo = new RunwaysInUseInfo();
    inUseInfo.current = getSuggestedThresholds();
    inUseInfo.scheduler = null;
  }

  public RunwayConfiguration getRunwayConfigurationInUse() {
    return inUseInfo.current;
  }

  public boolean isRunwayThresholdUnderMaintenance(ActiveRunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent()).isActive() == false;
    return ret;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return departureManager.getNumberOfPlanesAtHoldingPoint();
  }

  public RunwayConfiguration tryGetRunwayConfigurationScheduled() {
    return inUseInfo.scheduled;
  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane) {
    if (plane.isArrival()) {
      if (plane.getState() == Airplane.State.landed) {
        arrivalManager.unregisterFinishedArrival(plane);
      }
      //GO-AROUNDed planes are not unregistered, they have been unregistered previously
    }
    if (plane.isDeparture()) {
      departureManager.unregisterFinishedDeparture(plane);

      // add to stats
      ETime holdingPointEntryTime = departureManager.getAndEraseHoldingPointEntryTime(plane);
      int diffSecs = ETime.getDifference(Acc.now(), holdingPointEntryTime).getTotalSeconds();
      diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
      if (diffSecs < 0) diffSecs = 0;
      Acc.stats().registerDeparture(diffSecs);
    }

    if (plane.isEmergency() && plane.getState() == Airplane.State.landed) {
      // if it is landed emergency, close runway for amount of time
      ActiveRunway rwy = plane.getAssignedRunwayThresholdForLanding().getParent();
      RunwayCheck rwyCheck = RunwayCheck.createImmediateAfterEmergency();
      runwayChecks.set(rwy, rwyCheck);
    }
  }

  @Override
  public void removePlaneDeletedFromGame(Airplane plane) {
    if (plane.isArrival()) {
      arrivalManager.deletePlane(plane);
      //TODO this will add to stats even planes deleted from the game by a user(?)
      Acc.stats().registerArrival();
    }
    if (plane.isDeparture()) {
      departureManager.deletePlane(plane);
    }
  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean initialRegistration) {
    if (plane.isArrival())
      arrivalManager.registerNewArrival(plane);
    else {
      ActiveRunwayThreshold runwayThreshold = getRunwayThresholdForDeparture(plane);
      departureManager.registerNewDeparture(plane, runwayThreshold);
    }
  }

  public EventAnonymousSimple getOnRunwayChanged() {
    return onRunwayChanged;
  }

  public void setUpdatedWeatherFlag() {
    this.isUpdatedWeather = true;
  }

  private ActiveRunwayThreshold getRunwayThresholdForDeparture(Airplane plane) {
    ActiveRunwayThreshold ret;
    IList<ActiveRunwayThreshold> rts = inUseInfo.current.getDepartures()
        .where(q -> q.isForCategory(plane.getType().category))
        .select(q -> q.getThreshold());
    assert rts.size() > 0 : "No runway for airplane type " + plane.getType().name;
    ret = rts.getRandom();
    restrictToRunwaysNotUnderLongMaintenance(rts, true);
    if (rts.size() > 0)
      ret = rts.getRandom();
    return ret;
  }

  private void sendMessageToUser(String text) {
    Message msg = new Message(this, Acc.atcApp(),
        new StringMessageContent(text));
    super.sendMessage(msg);
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

  private void processMessageFromAtc(eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck rrct) {
    if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.askForTime) {
      RunwayCheck rc = this.runwayChecks.tryGet(rrct.runway);
      if (rc != null)
        announceScheduledRunwayCheck(rrct.runway, rc);
      else {
        for (ActiveRunway runway : this.runwayChecks.getKeys()) {
          rc = this.runwayChecks.get(runway);
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    } else if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.doCheck) {
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

  private void announceChangeRunwayInUse() {
    String msgTxt = this.inUseInfo.scheduled.toInfoString("\n");
    msgTxt = "Expected runway change to:\n" + msgTxt +
        "\nExpected runway change at " + this.inUseInfo.scheduler.getScheduledTime().toTimeString();
    sendMessageToUser(msgTxt);
  }

  private boolean isOnApproachOfTheRunwayInUse(Airplane p) {
    boolean ret = p.isEmergency() || inUseInfo.current.getArrivals()
        .isAny(q -> q.getThreshold().equals(p.tryGetCurrentApproachRunwayThreshold()) && q.isForCategory(p.getType().category));
    return ret;
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

  private void beginRunwayMaintenance(ActiveRunway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s is now in progress for approx %d minutes.", runway.getName(), rc.expectedDurationInMinutes);
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc.start();
  }

  private void finishRunwayMaintenance(ActiveRunway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s has ended.", runway.getName()
    );
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc = TowerAtc.RunwayCheck.createNormal(false);
    runwayChecks.set(runway, rc);
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

  private void checkForRunwayChange() {
    RunwayConfiguration newSuggested = getSuggestedThresholds();

    boolean isSame = inUseInfo.current.isUsingTheSameRunwayConfiguration(newSuggested);
    if (!isSame) {
      inUseInfo.scheduler = new SchedulerForAdvice(Acc.now().addSeconds(10 * 60), RWY_CHANGE_ANNOUNCE_INTERVALS);
      inUseInfo.scheduled = newSuggested;
    }
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

  private void restrictToRunwaysNotUnderLongMaintenance(IList<ActiveRunwayThreshold> rts, boolean onlyLongTimeMaintenance) {
    rts.remove(q -> {
      ActiveRunway r = q.getParent();
      TowerAtc.RunwayCheck rt = runwayChecks.get(r);
      boolean ret = rt.isActive() && rt.expectedDurationInMinutes > 5;
      return ret;
    });
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

  private boolean isRunwayThresholdHavingRecentDeparture(ActiveRunwayThreshold runwayThreshold) {
    boolean ret = departureManager.getLastDepartureTime(runwayThreshold).addSeconds(60).isAfterOrEq(Acc.now());
    return ret;
  }

  private boolean isRunwayOccupiedDueToDeparture(ActiveRunwayThreshold runwayThreshold) {
    boolean ret = false;
    ActiveRunwayThreshold rt = runwayThreshold;

    int clearAltitude = runwayThreshold.getParent().getParent().getAltitude() + 1500;
    ISet<ActiveRunwayThreshold> crts = inUseInfo.current.getCrossedSetForThreshold(rt);

    for (ActiveRunwayThreshold crt : crts) {
      Airplane lastDep = departureManager.tryGetTheLastDepartedPlane(crt);
      if (lastDep == null) continue; // no last departure
      if (lastDep.getAltitude() > clearAltitude) continue; // last departure has safe altitude
      double dist = Coordinates.getDistanceInNM(rt.getOtherThreshold().getCoordinate(), lastDep.getCoordinate());
      if (dist > 5) continue; // last departure has safe distance
      //boolean hasSafeSeparation = Separation.isSafeSeparation(lastDep, toPlane, (int) rt.getCourse(), 120);
      //if (hasSafeSeparation) continue; // has safe separation

      ret = true;
      break;
    }
    return ret;
  }

  private void tryTakeOffPlaneNew() {

    // checks for lined-up plane
    IMap<ActiveRunwayThreshold, Airplane> tmp = departureManager.getTheLinedUpPlanes();
    if (tmp.isEmpty()) return; // no-one is ready to departure
    Airplane toReadyPlane = null;
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

    ActiveRunwayThreshold availableThreshold = toReadyPlane.getExpectedRunwayThreshold();

    // if it gets here, the "toReadyPlane" can proceed take-off
    // add to stats
    departureManager.departAndGetHoldingPointEntryTime(toReadyPlane, availableThreshold, getDepartingPlaneSwitchAltitude(toReadyPlane.getType().category));
    toReadyPlane.setHoldingPointState(availableThreshold.getCoordinate(), availableThreshold.getCourse());

    SpeechList lst = new SpeechList();
    lst.add(new RadarContactConfirmationNotification());

    // TO altitude only when no altitude from SID already processed
    if (toReadyPlane.getTargetAltitude() <= availableThreshold.getParent().getParent().getAltitude())
      lst.add(new ChangeAltitudeCommand(
          ChangeAltitudeCommand.eDirection.climb, availableThreshold.getInitialDepartureAltitude()));

    lst.add(new ClearedForTakeoffCommand(availableThreshold));

    Message m = new Message(this, toReadyPlane, lst);
    super.sendMessage(m);
  }

  private boolean isRunwayUnderMaintenance(ActiveRunway runway) {
    return this.runwayChecks.get(runway).isActive();
  }

  private double getDepartingPlaneSwitchAltitude(char category) {
    switch (category) {
      case 'A':
        return (double) Acc.airport().getAltitude() + Acc.rnd().nextInt(100, 250);
      case 'B':
        return (double) Acc.airport().getAltitude() + Acc.rnd().nextInt(150, 400);
      case 'C':
      case 'D':
        return (double) Acc.airport().getAltitude() + Acc.rnd().nextInt(200, 750);
      default:
        throw new EEnumValueUnsupportedException(category);
    }
  }

}

class ArrivalManager {
  private IList<Airplane> landingPlanesList = new AirplaneList(true);
  private IList<Airplane> goAroundedPlanesToSwitchList = new AirplaneList(true);

  public double getClosestLandingPlaneDistanceForThreshold(ActiveRunwayThreshold threshold) {
    IList<Airplane> tmp = Acc.planes().where(q -> threshold.equals(q.tryGetAssignedRunwayThresholdForLanding()));
    double ret = Double.MAX_VALUE;
    for (Airplane plane : tmp) {
      if (plane.getState() == Airplane.State.landed) {
        ret = 0;
        break;
      } else if (plane.getState().is(
          Airplane.State.shortFinal,
          Airplane.State.longFinal,
          Airplane.State.approachDescend
      )) {
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), threshold.getCoordinate());
        if (dist < ret)
          ret = dist;
      }
    }
    return ret;
  }

  public void goAroundPlane(Airplane plane) {
    landingPlanesList.remove(plane);
    goAroundedPlanesToSwitchList.add(plane);
  }

  public boolean checkIfPlaneIsReadyToSwitchAndRemoveIt(Airplane plane) {
    if (goAroundedPlanesToSwitchList.contains(plane)) {
      goAroundedPlanesToSwitchList.remove(plane);
      return true;
    } else
      return false;
  }

  public void unregisterFinishedArrival(Airplane plane) {
    this.landingPlanesList.remove(plane);
  }

  public void deletePlane(Airplane plane) {
    this.landingPlanesList.tryRemove(plane);
    this.goAroundedPlanesToSwitchList.tryRemove(plane);
  }

  public void registerNewArrival(Airplane plane) {
    if (plane == null) {
      throw new IllegalArgumentException("Value of {plane} cannot not be null.");
    }

    assert plane.isArrival();
    assert plane.getAssignedRunwayThresholdForLanding() != null : "Assigned arrival for " + plane.getCallsign() + " is null.";
    if (plane.getState().is(Airplane.State.approachEnter, Airplane.State.approachDescend, Airplane.State.longFinal, Airplane.State.shortFinal))
      this.landingPlanesList.add(plane);
    else
      this.goAroundedPlanesToSwitchList.add(plane);
  }

  public boolean isSomeArrivalApproachingOrOnRunway(ActiveRunway runway) {
    if (runway == null) {
      throw new IllegalArgumentException("Value of {runway} cannot not be null.");
    }
    return this.landingPlanesList.where(q -> q.getAssignedRunwayThresholdForLanding().getParent().equals(runway)).isEmpty();
  }

  public boolean isSomeArrivalOnRunway(ActiveRunway rwy) {
    boolean ret = this.landingPlanesList
        .where(q -> rwy.getThresholds().contains(q.getAssignedRunwayThresholdForLanding()))
        .isAny(q -> q.getState() == Airplane.State.landed);
    return ret;
  }

  public void unregisterGoAroundedArrival(Airplane plane) {
    this.goAroundedPlanesToSwitchList.remove(plane);
  }
}

class DepartureManager {

  private final IList<Airplane> holdingPointNotReady = new AirplaneList(true);
  private final IList<Airplane> holdingPointReady = new AirplaneList(true);
  private final IList<Airplane> departing = new EList<>();
  private final IMap<Airplane, Double> departureSwitchAltitude = new EMap<>();
  private final IMap<Airplane, ETime> holdingPointWaitingTimeMap = new EMap<>();
  private final IMap<ActiveRunwayThreshold, Airplane> lastDepartingPlane = new EMap<>();
  private final IMap<ActiveRunwayThreshold, ETime> lastDeparturesTime = new EMap<>();

  public void registerNewDeparture(Airplane plane, ActiveRunwayThreshold runwayThreshold) {
    this.holdingPointNotReady.add(plane);
    holdingPointWaitingTimeMap.set(plane, Acc.now().clone());
    Route r = runwayThreshold.getDepartureRouteForPlane(plane.getType(), plane.getEntryExitFix(), true);
    plane.updateAssignedRouting(r, runwayThreshold);
  }

  public void confirmedByApproach(Airplane plane) {
    if (this.holdingPointNotReady.contains(plane)) {
      this.holdingPointNotReady.remove(plane);
      this.holdingPointReady.add(plane);
    }
  }

  public boolean canBeSwitched(Airplane plane) {
    if (departureSwitchAltitude.containsKey(plane) && departureSwitchAltitude.get(plane) < plane.getAltitude()) {
      departureSwitchAltitude.remove(plane);
      return true;
    } else
      return false;
  }

  public void departAndGetHoldingPointEntryTime(Airplane plane, ActiveRunwayThreshold th, double switchAltitude) {
    this.holdingPointReady.remove(plane);
    this.departing.add(plane);
    this.lastDepartingPlane.set(th, plane);
    this.lastDeparturesTime.set(th, Acc.now().clone());
    this.departureSwitchAltitude.set(plane, switchAltitude);
  }

  public ETime getAndEraseHoldingPointEntryTime(Airplane plane){
    ETime ret = holdingPointWaitingTimeMap.get(plane);
    holdingPointWaitingTimeMap.remove(plane);
    return ret;
  }

  public void unregisterFinishedDeparture(Airplane plane) {
    departing.remove(plane);
    lastDepartingPlane.remove(q->q.getValue() == plane);
  }

  public void deletePlane(Airplane plane) {
    holdingPointNotReady.tryRemove(plane);
    holdingPointReady.tryRemove(plane);
    departing.tryRemove(plane);
    for (ActiveRunwayThreshold rt : this.lastDepartingPlane.getKeys()) {
      if (this.lastDepartingPlane.containsKey(rt) && plane.equals(this.lastDepartingPlane.get(rt))) {
        this.lastDepartingPlane.set(rt, null);
        this.lastDeparturesTime.set(rt, null);
      }
    }
    holdingPointWaitingTimeMap.tryRemove(plane);
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return this.holdingPointNotReady.size() + this.holdingPointReady.size();
  }

  public boolean isSomeDepartureOnRunway(ActiveRunway runway) {
    for (ActiveRunwayThreshold rt : runway.getThresholds()) {
      Airplane aip = this.lastDepartingPlane.tryGet(rt);
      if (aip != null && aip.getState() == Airplane.State.takeOffRoll)
        return true;
    }
    return false;
  }

  public IMap<ActiveRunwayThreshold, Airplane> getTheLinedUpPlanes() {
    IMap<ActiveRunwayThreshold, Airplane> ret = new EMap<>();
    for (Airplane airplane : holdingPointReady) {
      if (ret.containsKey(airplane.getExpectedRunwayThreshold()) == false) {
        ret.set(airplane.getExpectedRunwayThreshold(), airplane);
      }
    }
    return ret;
  }


  public ETime getLastDepartureTime(ActiveRunwayThreshold rt) {
    ETime ret;
    ret = this.lastDeparturesTime.tryGet(rt);
    if (ret == null)
      ret = new ETime(0);
    return ret;
  }

  public Airplane tryGetTheLastDepartedPlane(ActiveRunwayThreshold rt) {
    Airplane ret;
    ret = this.lastDepartingPlane.tryGet(rt);
    return ret;
  }

}
