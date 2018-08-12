package eng.jAtcSim.lib.atcs;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.SchedulerForAdvice;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
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
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayConfiguration;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class TowerAtc extends ComputerAtc {

  public static class RunwayCheck {
    private int expectedDurationInMinutes;
    private ETime realDurationEnd;
    private SchedulerForAdvice scheduler;

    public static RunwayCheck createNormal(boolean isInitial) {
      int maxTime = 4 * 60;
      if (isInitial)
        maxTime = Acc.rnd().nextInt(maxTime);

      RunwayCheck ret = new RunwayCheck(maxTime, 5);
      return ret;
    }

    public static RunwayCheck createSnowCleaning(boolean isInitial) {
      int maxTime = Acc.rnd().nextInt(30, 180);
      if (isInitial)
        maxTime = Acc.rnd().nextInt(maxTime);

      RunwayCheck ret = new RunwayCheck(maxTime, 20);
      return ret;
    }

    public static RunwayCheck createImmediateAfterEmergency() {
      int closeDuration = Acc.rnd().nextInt(5, 33);
      RunwayCheck ret = new RunwayCheck(0, closeDuration);
      return ret;
    }

    private RunwayCheck() {
    }

    private RunwayCheck(int minutesToNextCheck, int expectedDurationInMinutes) {
      ETime et = Acc.now().addMinutes(minutesToNextCheck);
      this.scheduler = new SchedulerForAdvice(et);
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

  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;
  private final DepartureManager departureManager = new DepartureManager();
  private final ArrivalManager arrivalManager = new ArrivalManager();
  @XmlIgnore
  private final CommonRecorder toRecorder;
  @XmlIgnore
  private final EventAnonymousSimple onRunwayChanged = new EventAnonymousSimple();
  private RunwaysInUseInfo inUseInfo = null;
  private EMap<Runway, RunwayCheck> runwayChecks = null;
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
      RunwayThreshold rt = getSuggestedThresholdsRegardlessRunwayConfigurations();
      IList<RunwayThreshold> rts = rt.getParallelGroup();
      ret = RunwayConfiguration.createForThresholds(rts);
    }

    assert ret != null : "There must be runway configuration created.";

    return ret;
  }

  private static RunwayThreshold getSuggestedThresholdsRegardlessRunwayConfigurations() {
    Weather w = Acc.weather();
    RunwayThreshold rt = null;
    if (w.getWindSpeetInKts() <= MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY) {
      for (Runway r : Acc.airport().getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
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
      for (Runway r : Acc.airport().getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
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
    toRecorder = new CommonRecorder(template.getName() + " - TO", template.getName() + "_to.log", "\t");
  }

  @Override
  public void elapseSecond() {
    super.elapseSecond();

    tryTakeOffPlaneNew();
    processRunwayCheckBackground();
    processRunwayChangeBackground();
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
    departureManager.confirmByApproach(plane);

    if (departureManager.canBeSwitched(plane))
      return true;

    return false;
  }

  @Override
  protected ComputerAtc.RequestResult canIAcceptPlane(Airplane p) {
    if (p.isDeparture()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is a departure.", p.getCallsign()));
    }
    if (Acc.prm().getResponsibleAtc(p) != Acc.atcApp()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is not from APP.", p.getCallsign()));
    }
    if (isOnApproachOfTheRunwayInUse(p) == false) {
      return new ComputerAtc.RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", p.getCallsign()));
    }
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
    for (Runway runway : Acc.airport().getRunways()) {
      RunwayCheck rc = TowerAtc.RunwayCheck.createNormal(true);
      runwayChecks.set(runway, rc);
    }

    inUseInfo = new RunwaysInUseInfo();
    inUseInfo.scheduled = getSuggestedThresholds();
    inUseInfo.scheduler = new SchedulerForAdvice(Acc.now().clone());
    processRunwayChangeBackground();

    WeatherProvider wp = Acc.weatherProvider();
    wp.getWeatherUpdatedEvent().add(w -> weatherUpdated(w));
  }

  public boolean isRunwayThresholdUnderMaintenance(RunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent()).isActive() == false;
    return ret;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return departureManager.getNumberOfPlanesAtHoldingPoint();
  }

  public IReadOnlyList<RunwayThreshold> getRunwayThresholdsInUse(eDirection direction, char category) {
    IReadOnlyList<RunwayThreshold> ret;
    ret = getSuggestedRunwayThreshold(direction, category,
        inUseInfo.current.getDepartures(), inUseInfo.current.getArrivals());
    return ret;
  }

  public IReadOnlyList<RunwayThreshold> getRunwayThresholdsInUse(eDirection direction) {
    IReadOnlyList<RunwayThreshold> ret = inUseInfo.current.getDepartures().select(q->q.getThreshold());
    return ret;
  }

  public IReadOnlyList<RunwayThreshold> getRunwayThresholdsScheduled(eDirection direction, char category) {
    IReadOnlyList<RunwayThreshold> ret;
    if (inUseInfo.scheduled == null)
      ret = new EList<>();
    else {
      ret = getSuggestedRunwayThreshold(direction, category,
          inUseInfo.scheduled.getDepartures(), inUseInfo.scheduled.getArrivals());
    }
    return ret;
  }

  private static IReadOnlyList<RunwayThreshold> getSuggestedRunwayThreshold(eDirection direction, char category,
                                                                            IReadOnlyList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals,
                                                                            IReadOnlyList<RunwayConfiguration.RunwayThresholdConfiguration> departures) {
    IReadOnlyList<RunwayThreshold> ret;
    switch (direction) {
      case departures:
        ret = departures.where(q->q.isForCategory(category)).select(q -> q.getThreshold());
        break;
      case arrivals:
        if (arrivals.isAny(q->q.isPrimary()))
          ret = arrivals.where(q->q.isPrimary() && q.isForCategory(category)).select(q->q.getThreshold());
        else
          ret = arrivals.where(q->q.isForCategory(category)).select(q -> q.getThreshold());
        break;
      default:
        throw new EEnumValueUnsupportedException(direction);
    }
    return ret;
  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane) {
    if (plane.isArrival()) {
      if (plane.getState() == Airplane.State.landed)
        arrivalManager.unregisterFinishedArrival(plane);
      else
        arrivalManager.unregisterGoAroundedArrival(plane);
    }
    if (plane.isDeparture()) {
      departureManager.unregisterFinishedDeparture(plane);
    }

    if (plane.isEmergency() && plane.getState() == Airplane.State.landed) {
      // if it is landed emergency, close runway for amount of time
      Runway rwy = plane.getAssignedRunwayThreshold().getParent();
      RunwayCheck rwyCheck = RunwayCheck.createImmediateAfterEmergency();
      runwayChecks.set(rwy, rwyCheck);
    }
  }

  @Override
  public void removePlaneDeletedFromGame(Airplane plane) {
    if (plane.isArrival()) {
      arrivalManager.deletePlane(plane);
    }
    if (plane.isDeparture()) {
      departureManager.deletePlane(plane);
    }
  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean initialRegistration) {
    if (plane.isArrival())
      arrivalManager.registerNewArrival(plane);
    else
      departureManager.registerNewDeparture(plane);
  }

  public EventAnonymousSimple getOnRunwayChanged() {
    return onRunwayChanged;
  }

  private void processMessageFromAtc(RunwayUse ru) {
    EStringBuilder sb = new EStringBuilder();
    Message msg;
    sb.append("Runway(s) in use: ");
    sb.append(inUseInfo.current.toLineInfoString());
    msg = new Message(this, Acc.atcApp(),
        new StringMessageContent(sb.toString()));
    super.sendMessage(msg);

    if (inUseInfo.scheduled != null) {
      sb = new EStringBuilder();
      sb.append("Scheduled runways to use: ");
      sb.append(inUseInfo.scheduled.toLineInfoString());
      sb.append(" from ");
      sb.append(inUseInfo.scheduler.getScheduledTime().toHourMinuteString());
      msg = new Message(this, Acc.atcApp(),
          new StringMessageContent(sb.toString()));
      super.sendMessage(msg);
    }
  }

  private void processMessageFromAtc(eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck rrct) {
    if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.askForTime) {
      RunwayCheck rc = this.runwayChecks.tryGet(rrct.runway);
      if (rc != null)
        announceScheduledRunwayCheck(rrct.runway, rc);
      else {
        for (Runway runway : this.runwayChecks.getKeys()) {
          rc = this.runwayChecks.get(runway);
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    } else if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.doCheck) {
      Runway rwy = rrct.runway;
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

  private void weatherUpdated(Weather w) {
    this.isUpdatedWeather = true;
  }

  private void processRunwayChangeBackground() {
    if (inUseInfo.scheduler == null) {
      if (isUpdatedWeather) {
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
    EStringBuilder sb = new EStringBuilder();

    sb.append("Expected runway change to: ");
    sb.append(this.inUseInfo.scheduled.toLineInfoString());
    sb.appendFormat(" at %s.", this.inUseInfo.scheduler.getScheduledTime().toTimeString());

    Message m = new Message(
        this,
        Acc.atcApp(),
        new StringMessageContent(sb.toString()));
    super.sendMessage(m);
  }

  private boolean isOnApproachOfTheRunwayInUse(Airplane p) {
    boolean ret = inUseInfo.current.getArrivals()
        .isAny(q -> q.getThreshold().equals(p.tryGetCurrentApproachRunwayThreshold()));
    return ret;
  }

  private void processRunwayCheckBackground() {
    for (Runway runway : runwayChecks.getKeys()) {
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

  private void beginRunwayMaintenance(Runway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s is now in progress for approx %d minutes.", runway.getName(), rc.expectedDurationInMinutes);
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc.start();
  }

  private void finishRunwayMaintenance(Runway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s has ended.", runway.getName()
    );
    Message m = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(m);

    rc = TowerAtc.RunwayCheck.createNormal(true);
    runwayChecks.set(runway, rc);
  }

  private void announceScheduledRunwayCheck(Runway rwy, RunwayCheck rc) {
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
      inUseInfo.scheduler = new SchedulerForAdvice(Acc.now().addSeconds(10 * 60));
      inUseInfo.scheduled = newSuggested;
    }
  }

  private void changeRunwayInUse() {

    EStringBuilder str = new EStringBuilder();
    str.appendLine("Changed runway(s) in use now to: ");
    str.append(inUseInfo.scheduled.toLineInfoString());
    str.append(".");

    Message m = new Message(
        this,
        Acc.atcApp(),
        StringResponse.create(str.toString()));
    super.sendMessage(m);
    this.inUseInfo.current = this.inUseInfo.scheduled;
    this.inUseInfo.scheduled = null;
    this.inUseInfo.scheduler = null;

    IList<Runway> tmp =
        this.inUseInfo.current.getDepartures()
            .select(q -> q.getThreshold().getParent())
            .union(this.inUseInfo.current.getArrivals().select(q -> q.getThreshold().getParent()));
    tmp.forEach(q -> announceScheduledRunwayCheck(q, this.runwayChecks.get(q)));

    onRunwayChanged.raise();
  }

  private void tryToLog(String format, Object... params) {
    if (toRecorder != null)
      toRecorder.write(format, params);
  }

  private void restrictToRunwaysNotUnderMaintenance(IList<RunwayThreshold> rts) {
    rts.remove(q -> runwayChecks.get(q.getParent()).isActive());
  }

  private void restrictToRunwaysNotUsedInApproach(IList<RunwayThreshold> rts) {
    rts.remove(q -> arrivalManager.getClosestLandingPlaneDistanceForThreshold(q) < 2.5);
  }

  private void restrictToRunwaysNotUsedByRolling(IList<RunwayThreshold> rts) {
    IList<RunwayThreshold> tmp = new EList<>();
    for (RunwayThreshold rt : rts) {
      ISet<RunwayThreshold> crossedSet = inUseInfo.current.getCrossedSetForThreshold(rt);
      boolean hasSetAirplaneOnGround = crossedSet.isAny(q -> hasAirplaneRollingOnTheGround(q));
      if (hasSetAirplaneOnGround)
        tmp.add(crossedSet);
    }
    rts.tryRemove(tmp);
  }

  private boolean hasAirplaneRollingOnTheGround(RunwayThreshold rt) {
    boolean ret;
    ret = arrivalManager.isSomeArrivalOnRunway(rt.getParent());
    if (ret == false) {
      ret = departureManager.isSomeDepartureOnRunway(rt.getParent());
    }

    return ret;
  }

  private void restrictToRunwayAvailableForTakeOff(IList<RunwayThreshold> rts, Airplane toPlane) {
    IList<RunwayThreshold> toRem = new EList<>();

    for (RunwayThreshold rt : rts) {
      ETime t = departureManager.getLastDepartureTime(rt);
      t = t.addSeconds(60);
      if (t.isAfterOrEq(Acc.now()))
        toRem.add(rt);
    }
    rts.remove(toRem);
    toRem.clear();

//    rts.remove(q -> departureManager.getLastDepartureTime(q).addSeconds(60).isAfterOrEq(Acc.now()));
    if (rts.isEmpty()) return;

    double toPlaneClearAltitude = toPlane.getAltitude() + 1500;

    toRem = new EList<>();
    boolean removed;
    for (RunwayThreshold rt : rts) {
      removed = false;
      System.out.println("## checking " + rt.getName());
      Airplane lastDep = departureManager.getLastDeparturePlane(rt);
      if (lastDep != null) {
        System.out.println("## last-dep: " + lastDep.getCallsign().toString());
        System.out.println("## ... its altitude " + lastDep.getAltitude() + "/ target altitude: " + toPlaneClearAltitude);
        if (lastDep.getAltitude() < toPlaneClearAltitude) {
          double dist = Coordinates.getDistanceInNM(rt.getCoordinate(), lastDep.getCoordinate());
          System.out.println("## ... dist: " + dist);
          if (dist < 5) {
            removed = true;
            toRem.add(rt);
          }
        }
        if (!removed) {
          System.out.println("## ... safe-sep check");
          boolean safeSep = !Separation.isSafeSeparation(lastDep, toPlane, (int) rt.getCourse(), 120);
          System.out.println("## ... safe-sep-check-test-result: " + safeSep);
          if (safeSep)
            toRem.add(rt);
        }
      }
    }
    rts.remove(toRem);
  }

  private void tryTakeOffPlaneNew() {

    // checks for lined-up plane
    Airplane toReadyPlane = departureManager.tryGetPlaneReadyForTakeOff();
    if (toReadyPlane == null) return;

    IList<RunwayThreshold> rts = inUseInfo.current.getDepartures()
        .where(q -> q.isForCategory(toReadyPlane.getType().category))
        .select(q -> q.getThreshold());
    restrictToRunwaysNotUnderMaintenance(rts);
    restrictToRunwaysNotUsedInApproach(rts);
    restrictToRunwaysNotUsedByRolling(rts);
    restrictToRunwayAvailableForTakeOff(rts, toReadyPlane);

    if (rts.isEmpty())
      return; // no available rwy

    RunwayThreshold availableThreshold = rts.getRandom();
    // plane has SID for different threshold
    // may occur in runway change or parallel runways
    // first try to getContent route for the same navaid, then try to find any route
    Route r = availableThreshold.getRoutes().tryGetFirst(q ->
        q.getType() == Route.eType.sid &&
            q.getMainNavaid().equals(toReadyPlane.getEntryExitFix()) &&
            q.isValidForCategory(toReadyPlane.getType().category));
    if (r == null)
      r = availableThreshold.getRoutes().where(q ->
          q.getType() == Route.eType.sid &&
              q.isValidForCategory(toReadyPlane.getType().category)).getRandom();
    toReadyPlane.updateAssignedRoute(r);

    // if it gets here, the "toReadyPlane" can proceed take-off
    ETime holdingPointEntryTime = departureManager.departAndGetHoldingPointEntryTime(toReadyPlane, availableThreshold, getDepartingPlaneSwitchAltitude(toReadyPlane.getType().category));
    toReadyPlane.setHoldingPointState(availableThreshold.getCoordinate(), availableThreshold.getCourse());

    // add to stats
    double diffSecs = ETime.getDifference(Acc.now(), holdingPointEntryTime).getTotalSeconds();
    diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
    if (diffSecs < 0) diffSecs = 0;
    Acc.stats().holdingPointInfo.maximumHoldingPointTime.set(diffSecs);
    Acc.stats().holdingPointInfo.meanHoldingPointTime.add(diffSecs);

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

class Separation {
  private static final int SAFE_SEPARATION_ALTITUDE = 2000;
  private static final double SAFE_SEPARATION_DISTANCE = 5.5;

  public static boolean isSafeSeparation(Airplane flyingPlane, Airplane readyPlane, int readyPlaneRunwayHeading, int safeSeparationSeconds) {
    int aSeconds = getSecondsInFlight(flyingPlane, safeSeparationSeconds);
    int bSeconds = getSecondsInFlight(readyPlane, safeSeparationSeconds);
    Coordinate aTargetPosition = getPosition(flyingPlane, aSeconds, (int) flyingPlane.getHeading());
    Coordinate bTargetPosition = getPosition(readyPlane, bSeconds, readyPlaneRunwayHeading); // flyingPLane.getHeading() is correct as we need both planes to estimate same headings
    double dist = Coordinates.getDistanceInNM(aTargetPosition, bTargetPosition);
    int aTargetAlt = getAltitude(flyingPlane, aSeconds, true);
    int bTargetAlt = getAltitude(readyPlane, bSeconds, false);
    double alt = aTargetAlt - bTargetAlt;
    if (dist > SAFE_SEPARATION_DISTANCE)
      return true;
    else if (alt > SAFE_SEPARATION_ALTITUDE)
      return true;
    else
      return false;
  }

  private static int getSecondsInFlight(Airplane plane, int totalSeconds) {
    double spdRef = plane.getType().vCruise - plane.getSpeed();
    double secondsToAccelerate = spdRef / plane.getType().speedIncreaseRate;
    int ret = totalSeconds - (int) secondsToAccelerate;
    if (ret < 0) ret = 0;
    return ret;
  }

  private static int getAltitude(Airplane plane, int seconds, boolean isHigher) {
    double refAlt = isHigher ? plane.getAltitude() + SAFE_SEPARATION_ALTITUDE : plane.getAltitude();
    double ret = plane.getAltitude() + plane.getType().getClimbRateForAltitude(refAlt) * seconds;
    return (int) ret;
  }

  private static Coordinate getPosition(Airplane plane, int seconds, int heading) {
    double traveledDistance = seconds / 3600d * plane.getType().getV2();
    Coordinate ret = Coordinates.getCoordinate(plane.getCoordinate(), heading, traveledDistance);
    return ret;
  }
}

class ArrivalManager {
  private IList<Airplane> landingPlanesList = new AirplaneList(true);
  private IList<Airplane> goAroundedPlanesToSwitchList = new AirplaneList(true);

  public double getClosestLandingPlaneDistanceForThreshold(RunwayThreshold threshold) {
    IList<Airplane> tmp = Acc.planes().where(q -> threshold.equals(q.tryGetAssignedRunwayThreshold()));
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

    assert plane.getAssignedRunwayThreshold() != null : "Assigned arrival for " + plane.getCallsign() + " is null.";
    this.landingPlanesList.add(plane);
  }

  public boolean isSomeArrivalApproachingOrOnRunway(Runway runway) {
    if (runway == null) {
      throw new IllegalArgumentException("Value of {runway} cannot not be null.");
    }
    return this.landingPlanesList.where(q -> q.getAssignedRunwayThreshold().getParent().equals(runway)).isEmpty();
  }

  public boolean isSomeArrivalOnRunway(Runway rwy) {
    boolean ret = this.landingPlanesList
        .where(q -> rwy.getThresholds().contains(q.getAssignedRunwayThreshold()))
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
  private final IMap<RunwayThreshold, Airplane> lastDepartures = new EMap<>();
  private final IMap<RunwayThreshold, ETime> lastDeparturesTime = new EMap<>();

  public void registerNewDeparture(Airplane plane) {
    this.holdingPointNotReady.add(plane);
    holdingPointWaitingTimeMap.set(plane, Acc.now().clone());
  }

  public void confirmByApproach(Airplane plane) {
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

  public ETime departAndGetHoldingPointEntryTime(Airplane plane, RunwayThreshold th, double switchAltitude) {
    this.holdingPointReady.remove(plane);
    this.departing.add(plane);
    this.lastDepartures.set(th, plane);
    this.lastDeparturesTime.set(th, Acc.now().clone());
    this.departureSwitchAltitude.set(plane, switchAltitude);

    ETime ret = holdingPointWaitingTimeMap.get(plane);
    holdingPointWaitingTimeMap.remove(plane);
    return ret;
  }

  public void unregisterFinishedDeparture(Airplane plane) {
    departing.remove(plane);
  }

  public void deletePlane(Airplane plane) {
    holdingPointNotReady.tryRemove(plane);
    holdingPointReady.tryRemove(plane);
    departing.tryRemove(plane);
    for (RunwayThreshold rt : this.lastDepartures.getKeys()) {
      if (this.lastDepartures.containsKey(rt) && this.lastDepartures.get(rt).equals(plane)) {
        this.lastDepartures.set(rt, null);
        this.lastDeparturesTime.set(rt, null);
      }
    }
    holdingPointWaitingTimeMap.tryRemove(plane);
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return this.holdingPointNotReady.size() + this.holdingPointReady.size();
  }

  public boolean isSomeDepartureOnRunway(Runway runway) {
    for (RunwayThreshold rt : runway.getThresholds()) {
      Airplane aip = this.lastDepartures.tryGet(rt);
      if (aip != null && aip.getState() == Airplane.State.takeOffRoll)
        return true;
    }
    return false;
  }

  public Airplane tryGetPlaneReadyForTakeOff() {
    Airplane ret = holdingPointReady.tryGet(0);
    return ret;
  }


  public ETime getLastDepartureTime(RunwayThreshold rt) {
    ETime ret;
    ret = this.lastDeparturesTime.tryGet(rt);
    if (ret == null)
      ret = new ETime(0);
    return ret;
  }

  public Airplane getLastDeparturePlane(RunwayThreshold rt) {
    Airplane ret;
    ret = this.lastDepartures.tryGet(rt);
    return ret;
  }

}
