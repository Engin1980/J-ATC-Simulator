package eng.jAtcSim.newLib.atcs.internal.tower;


import eng.eSystem.collections.*;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.atcs.internal.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.InternalAcc;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedForTakeoffCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.TaxiToHoldingPointCommand;
import eng.jAtcSim.newLib.speeches.atc2atc.RunwayCheck;
import eng.jAtcSim.newLib.speeches.atc2atc.RunwayUse;
import eng.jAtcSim.newLib.speeches.atc2atc.StringResponse;
import eng.jAtcSim.newLib.stats.StatsAcc;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherAcc;

public class TowerAtc extends ComputerAtc {


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
  private EMap<String, RunwayCheckInfo> runwayChecks = null;
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
      RunwayCheckInfo rc = RunwayCheckInfo.createNormal(true);
      runwayChecks.set(runway.getName(), rc);
    }

    inUseInfo = new RunwaysInUseInfo();
    inUseInfo.current = getSuggestedThresholds();
    inUseInfo.scheduler = null;
  }

  public boolean isRunwayThresholdUnderMaintenance(ActiveRunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent().getName()).isActive() == false;
    return ret;
  }

  @Override
  public void registerNewPlaneUnderControl(Callsign callsign, boolean initialRegistration) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival())
      arrivalManager.registerNewArrival(plane);
    else {
      ActiveRunwayThreshold runwayThreshold = getRunwayThresholdForDeparture(plane);
      departureManager.registerNewDeparture(plane, runwayThreshold);
    }
  }

  @Override
  public void removePlaneDeletedFromGame(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isArrival()) {
      arrivalManager.deletePlane(plane);
      //TODO this will add to stats even planes deleted from the game by a user(?)
      //TODO this stats value should be increased outside of Tower atc??
      StatsAcc.getOverallStatsWriter().registerArrival();
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
  public void unregisterPlaneUnderControl(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
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
      StatsAcc.getOverallStatsWriter().registerDeparture(diffSecs);
    }

    if (plane.isEmergency() && plane.getState() == AirplaneState.landed) {
      // if it is landed emergency, close runway for amount of time
      ActiveRunway rwy = plane.getRouting().getAssignedRunwayThreshold().getParent();
      RunwayCheckInfo rwyCheck = RunwayCheckInfo.createImmediateAfterEmergency();
      runwayChecks.set(rwy.getName(), rwyCheck);
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
  protected boolean acceptsNewRouting(Callsign callsign, SwitchRoutingRequest srr) {
    IAirplane plane = InternalAcc.getPlane(callsign);
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

  private void announceScheduledRunwayCheck(String runwayName, RunwayCheckInfo rc) {
    StringResponse cnt;
    if (rc.isActive())
      cnt = StringResponse.create("Runway %s is under maintenance right now until approximately %s.",
          runwayName,
          rc.getRealDurationEnd().toHourMinuteString()
      );
    else {
      cnt = StringResponse.create("Runway %s maintenance is scheduled at %s for approximately %d minutes.",
          runwayName,
          rc.getScheduler().getScheduledTime().toHourMinuteString(),
          rc.getExpectedDurationInMinutes());
      rc.getScheduler().nowAnnounced();
    }

    Message msg = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(InternalAcc.getApp().getAtcId()),
        cnt);
    super.sendMessage(msg);

  }

  private void beginRunwayMaintenance(String rwyName, RunwayCheckInfo rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s is now in progress for approx %d minutes.",
        rwyName, rc.getExpectedDurationInMinutes());
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(InternalAcc.getApp().getAtcId()),
        cnt);
    super.sendMessage(m);

    rc.start();
  }

  @Override
  protected ComputerAtc.RequestResult canIAcceptPlane(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    if (plane.isDeparture()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is a departure.", plane.getCallsign()));
    }
    if (InternalAcc.getPrm().forAtc().getResponsibleAtc(callsign).equals(InternalAcc.getApp().getAtcId())) {
      return new ComputerAtc.RequestResult(false, String.format("%s is not from APP.", plane.getCallsign()));
    }
    if (isOnApproachOfTheRunwayInUse(plane) == false)
      return new ComputerAtc.RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", plane.getCallsign()));
    if (isRunwayThresholdUnderMaintenance(plane.getRouting().getAssignedRunwayThreshold()) == false) {
      return new RequestResult(false, String.format("Runway %s is closed now.", plane.getRouting().getAssignedRunwayThreshold().getParent().getName()));
    }
    if (plane.getSha().getAltitude() > this.getAcceptAltitude()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too high.", plane.getCallsign()));
    }
    double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), AreaAcc.getAirport().getLocation());
    if (dist > MAXIMAL_ACCEPT_DISTANCE_IN_NM) {
      return new ComputerAtc.RequestResult(false, String.format("%s is too far.", plane.getCallsign()));
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
    tmp.forEach(q -> announceScheduledRunwayCheck(q.getName(), this.runwayChecks.get(q.getName())));

    onRunwayChanged.raise();
  }

  private void checkForRunwayChange() {
    RunwayConfiguration newSuggested = getSuggestedThresholds();

    boolean isSame = inUseInfo.current.isUsingTheSameRunwayConfiguration(newSuggested);
    if (!isSame) {
      inUseInfo.scheduler = new SchedulerForAdvice(SharedAcc.getNow().addSeconds(10 * 60), RWY_CHANGE_ANNOUNCE_INTERVALS);
      inUseInfo.scheduled = newSuggested;
    }
  }

  private void finishRunwayMaintenance(String rwyName, RunwayCheckInfo rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s has ended.", rwyName
    );
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(InternalAcc.getApp().getAtcId()),
        cnt);
    super.sendMessage(m);

    rc = RunwayCheckInfo.createNormal(false);
    runwayChecks.set(rwyName, rc);
  }

  private double getDepartingPlaneSwitchAltitude(char category) {
    switch (category) {
      case 'A':
        return (double) AreaAcc.getAirport().getAltitude() + SharedAcc.getRnd().nextInt(100, 250);
      case 'B':
        return (double) AreaAcc.getAirport().getAltitude() + SharedAcc.getRnd().nextInt(150, 400);
      case 'C':
      case 'D':
        return (double) AreaAcc.getAirport().getAltitude() + SharedAcc.getRnd().nextInt(200, 750);
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
  protected AtcId getTargetAtcIfPlaneIsReadyToSwitch(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
    AtcId ret = null;
    if (this.arrivalManager.checkIfPlaneIsReadyToSwitchAndRemoveIt(plane)) {
      ret = InternalAcc.getApp().getAtcId();
    } else if (plane.isDeparture()) {
      ret = InternalAcc.getApp().getAtcId();
    }
    return ret;
  }

  private boolean isOnApproachOfTheRunwayInUse(IAirplane plane) {
    boolean ret = plane.isEmergency() || inUseInfo.current.getArrivals()
        .isAny(q -> q.getThreshold().equals(plane.getRouting().getAssignedRunwayThreshold()) && q.isForCategory(plane.getType().category));
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
                || departureManager.isSomeDepartureOnRunway(rt.getParent().getName()));
    return ret;
  }

  private boolean isRunwayOccupiedDueToDeparture(ActiveRunwayThreshold runwayThreshold) {
    boolean ret = false;
    ActiveRunwayThreshold rt = runwayThreshold;

    int clearAltitude = runwayThreshold.getParent().getParent().getAltitude() + 1500;
    ISet<ActiveRunwayThreshold> crts = inUseInfo.current.getCrossedSetForThreshold(rt);

    for (ActiveRunwayThreshold crt : crts) {
      IAirplane lastDep = departureManager.tryGetTheLastDepartedPlane(crt);
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
    boolean ret = departureManager.getLastDepartureTime(runwayThreshold).addSeconds(60).isAfterOrEq(SharedAcc.getNow());
    return ret;
  }

  private boolean isRunwayUnderMaintenance(ActiveRunway runway) {
    return this.runwayChecks.get(runway.getName()).isActive();
  }

  private void processMessageFromAtc(RunwayUse ru) {

    if (ru.isAsksForChange()) {
      if (inUseInfo.scheduled == null) {
        Message msg = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(InternalAcc.getApp().getAtcId()),
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

  private void processMessageFromAtc(RunwayCheck rrct) {
    if (rrct.type == RunwayCheck.eType.askForTime) {
      RunwayCheckInfo rc = this.runwayChecks.tryGet(rrct.runway);
      if (rc != null)
        announceScheduledRunwayCheck(rrct.runway, rc);
      else {
        for (String runwayName : this.runwayChecks.getKeys()) {
          rc = this.runwayChecks.get(runwayName);
          announceScheduledRunwayCheck(runwayName, rc);
        }
      }
    } else if (rrct.type == RunwayCheck.eType.doCheck) {
      //ActiveRunway rwy = rrct.runway;
      String rwyName = rrct.runway;
      RunwayCheckInfo rc = this.runwayChecks.tryGet(rwyName);
      if (rwyName == null && this.runwayChecks.size() == 1) {
        rwyName = this.runwayChecks.getKeys().getFirst();
        rc = this.runwayChecks.get(rwyName);
      }
      if (rc == null) {
        Message msg = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(InternalAcc.getApp().getAtcId()),
            StringResponse.createRejection("Sorry, you must specify exact runway (threshold) at which I can start the maintenance."));
        super.sendMessage(msg);
      } else {
        if (rc.isActive()) {
          Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAtc(InternalAcc.getApp().getAtcId()),
              StringResponse.createRejection("The runway %s is already under maintenance right now.",
                  rwyName));
          super.sendMessage(msg);
        } else if (rc.getScheduler().getMinutesLeft() > 30) {
          Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAtc(InternalAcc.getApp().getAtcId()),
              StringResponse.createRejection("Sorry, the runway %s is scheduled for the maintenance in more than 30 minutes.",
                  rwyName));
          super.sendMessage(msg);
        } else {
          Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAtc(InternalAcc.getApp().getAtcId()),
              StringResponse.create("The maintenance of the runway %s is approved and will start shortly.",
                  rwyName));
          super.sendMessage(msg);
          rc.getScheduler().setApprovedTrue();
        }
      }
    }
  }

  @Override
  protected void processMessagesFromPlane(Callsign callsign, SpeechList spchs) {
    if (spchs.containsType(GoingAroundNotification.class)) {
      arrivalManager.goAroundPlane(callsign);
    }
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    if (m.getContent() instanceof RunwayCheckInfo) {
      RunwayCheck rrct = m.getContent();
      processMessageFromAtc(rrct);
    } else if (m.getContent() instanceof RunwayUse) {
      RunwayUse ru = m.getContent();
      processMessageFromAtc(ru);
    }
  }

  private void processRunwayChangeBackground() {
    if (inUseInfo.scheduler == null) {
      if (isUpdatedWeather) {
        if (WeatherAcc.getWeather().getSnowState() != Weather.eSnowState.none)
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
    for (String runwayName : runwayChecks.getKeys()) {
      RunwayCheckInfo rc = runwayChecks.get(runwayName);
      if (rc.isActive()) {
        if (rc.getRealDurationEnd().isBeforeOrEq(SharedAcc.getNow()))
          finishRunwayMaintenance(runwayName, rc);
      } else {
        if (rc.getScheduler().isElapsed()) {
          if (this.departureManager.isSomeDepartureOnRunway(runwayName) == false
              && this.arrivalManager.isSomeArrivalApproachingOrOnRunway(runwayName))
            beginRunwayMaintenance(runwayName, rc);
        } else if (rc.getScheduler().shouldBeAnnouncedNow()) {
          announceScheduledRunwayCheck(runwayName, rc);
        }
      }
    }
  }

  private void restrictToRunwaysNotUnderLongMaintenance(IList<ActiveRunwayThreshold> rts, boolean onlyLongTimeMaintenance) {
    rts.remove(q -> {
      ActiveRunway r = q.getParent();
      RunwayCheckInfo rt = runwayChecks.get(r.getName());
      boolean ret = rt.isActive() && rt.getExpectedDurationInMinutes() > 5;
      return ret;
    });
  }

  private void sendMessageToUser(String text) {
    Message msg = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(InternalAcc.getApp().getAtcId()),
        new StringMessageContent(text));
    super.sendMessage(msg);
  }

  @Override
  protected boolean shouldBeSwitched(Callsign callsign) {
    IAirplane plane = InternalAcc.getPlane(callsign);
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

    SpeechList<ISpeech> lst = new SpeechList<>();
    lst.add(new RadarContactConfirmationNotification());
    lst.add(TaxiToHoldingPointCommand.create(availableThreshold.getName()));

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
    for (String rwyName : this.runwayChecks.getKeys()) {
      RunwayCheckInfo rc = this.runwayChecks.get(rwyName);
      if (rc.isActive()) continue;
      int maxInterval = WeatherAcc.getWeather().getSnowState() == Weather.eSnowState.intensive
          ? RunwayCheckInfo.MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL
          : RunwayCheckInfo.MAX_SNOW_MAINTENANCE_INTERVAL;
      if (rc.getScheduler().getMinutesLeft() > maxInterval) {
        rc = RunwayCheckInfo.createSnowCleaning(
            false,
            WeatherAcc.getWeather().getSnowState() == Weather.eSnowState.intensive);
        runwayChecks.set(rwyName, rc);
        announceScheduledRunwayCheck(rwyName, rc);
      }
    }
  }

}