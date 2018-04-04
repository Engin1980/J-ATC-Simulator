package eng.jAtcSim.lib.atcs;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.SchedulerForAdvice;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayUse;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.StringResponse;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<RunwayThreshold> current;
    private List<RunwayThreshold> scheduled;

    public boolean isInUse(RunwayThreshold threshold) {
      return current.contains(threshold);
    }

  }

  private static final int RUNWAY_CHANGE_INFO_UPDATE_INTERVAL = 5 * 60;
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;
  private final TakeOffInfos takeOffInfos = new TakeOffInfos();
  private final CommonRecorder toRecorder;
  private AirplaneList landingPlanesList = new AirplaneList();
  private AirplaneList goAroundedPlanesToSwitchList = new AirplaneList();
  private AirplaneList holdingPointPlanesList = new AirplaneList();
  private AirplaneList linedUpPlanesList = new AirplaneList();
  private AirplaneList departingPlanesList = new AirplaneList();
  private Map<Airplane, ETime> holdingPointWaitingTimeMap = new HashMap<>();
  private RunwaysInUseInfo inUseInfo = null;
  private EMap<Runway, RunwayCheck> runwayChecks = null;
  private boolean isUpdatedWeather;

  private static List<RunwayThreshold> getSuggestedThresholds() {
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

    List<RunwayThreshold> ret = rt.getParallelGroup();
    return ret;
  }

  public TowerAtc(AtcTemplate template) {
    super(template);
    toRecorder = new CommonRecorder(template.getName() + " - TO", template.getName() + "_to.log", "\t");
  }

  @Override
  public void unregisterPlaneUnderControl(Airplane plane, boolean finalUnregistration) {
    //TODO the Tower ATC does some unregistration operations probably somewhere here in the code, should be checked
    if (landingPlanesList.contains(plane))
      landingPlanesList.remove(plane);
    if (goAroundedPlanesToSwitchList.contains(plane))
      goAroundedPlanesToSwitchList.remove(plane);
    if (holdingPointPlanesList.contains(plane)) {
      holdingPointPlanesList.remove(plane);
    }
    if (linedUpPlanesList.contains(plane))
      linedUpPlanesList.remove(plane);
    if (departingPlanesList.contains(plane))
      departingPlanesList.remove(plane);
    if (holdingPointWaitingTimeMap.containsKey(plane))
      holdingPointWaitingTimeMap.remove(plane);


    IMap<RunwayThreshold, TakeOffInfo> tmp = takeOffInfos.whereValue(q -> q.airplane == plane);
    tmp.keySet().forEach(q -> takeOffInfos.remove(q));
  }

  @Override
  public void registerNewPlaneUnderControl(Airplane plane, boolean initialRegistration) {
    if (plane.isArrival()) {
      landingPlanesList.add(plane);
    } else {
      holdingPointPlanesList.add(plane);
      holdingPointWaitingTimeMap.put(plane, Acc.now().clone());
    }
  }

  @Override
  public void elapseSecond() {
    super.elapseSecond();

    tryTakeOffPlane();
    processRunwayCheckBackground();
    processRunwayChangeBackground();
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
    wp.getWeatherUpdatedEvent().add(() -> weatherUpdated());
  }

  @Override
  protected void processMessageFromAtc(Message m) {
    if (m.getContent() instanceof eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck) {
      eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck rrct = m.getContent();
      processMessageFromAtc(rrct);
    } else if (m.getContent() instanceof RunwayUse) {
      RunwayUse ru = m.getContent();
      processMessageFromAtc(ru);
    }
  }

  private void processMessageFromAtc(RunwayUse ru) {
    EStringBuilder sb = new EStringBuilder();
    sb.append("Runway(s) in use: ");
    sb.appendItems(inUseInfo.current, q->q.getName(), ", ");
    Message msg = new Message(this, Acc.atcApp(),
        new StringMessageContent(sb.toString()));
    super.sendMessage(msg);

    if (inUseInfo.scheduled != null){
      sb = new EStringBuilder();
      sb.append("Scheduled runway change to ");
      sb.appendItems(inUseInfo.scheduled, q->q.getName(), ", ");
      sb.append(" at ");
      sb.append(inUseInfo.scheduler.getScheduledTime().toString());

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
        for (Runway runway : this.runwayChecks.keySet()) {
          rc = this.runwayChecks.get(runway);
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    } else if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.doCheck) {
      Runway rwy = rrct.runway;
      RunwayCheck rc = this.runwayChecks.tryGet(rwy);
      if (rwy == null && this.runwayChecks.size() == 1) {
        rwy = inUseInfo.current.get(0).getParent();
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
  protected boolean shouldBeSwitched(Airplane plane) {
    if (plane.isArrival())
      return true; // this should be go-arounded arrivals

    // as this plane is asked for switch, it is confirmed
    // from APP, so can be moved from holding-point to line-up
    if (holdingPointPlanesList.contains(plane)) {
      holdingPointPlanesList.remove(plane);
      linedUpPlanesList.add(plane);
    }

    for (TakeOffInfo toi : takeOffInfos.values()) {
      if (toi.airplane == plane && toi.airplane.getAltitude() > toi.randomReadyToSwitchAltitude) {
        return true; // true = airplane can be switched
      }
    }

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
      landingPlanesList.remove(plane);
      goAroundedPlanesToSwitchList.add(plane);
    }
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(Airplane plane) {
    Atc ret = null;
    if (this.goAroundedPlanesToSwitchList.contains(plane)) {
      this.goAroundedPlanesToSwitchList.remove(plane);
      ret = Acc.atcApp();
    } else if (plane.isDeparture()) {
      ret = Acc.atcApp();
    }
    return ret;
  }

  public boolean isRunwayThresholdUnderMaintenance(RunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent()).isActive() == false;
    return ret;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return holdingPointPlanesList.size() + linedUpPlanesList.size();
  }

  public List<RunwayThreshold> getRunwayThresholdsInUse() {
    List<RunwayThreshold> ret = new ArrayList<>(inUseInfo.current);
    return ret;
  }

  private void weatherUpdated() {
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
    sb.appendItems(this.inUseInfo.scheduled, q -> q.getName(), ", ");
    sb.appendFormat(" at %s.", this.inUseInfo.scheduler.getScheduledTime().toTimeString());

    Message m = new Message(
        this,
        Acc.atcApp(),
        new StringMessageContent(sb.toString()));
    super.sendMessage(m);
  }

  private boolean isOnApproachOfTheRunwayInUse(Airplane p) {
    boolean ret = inUseInfo.isInUse(p.tryGetCurrentApproachRunwayThreshold());
    return ret;
  }

  private void processRunwayCheckBackground() {
    for (Runway runway : runwayChecks.keySet()) {
      RunwayCheck rc = runwayChecks.get(runway);
      if (rc.isActive()) {
        if (rc.realDurationEnd.isBeforeOrEq(Acc.now()))
          finishRunwayMaintenance(runway, rc);
      } else {
        if (rc.scheduler.isElapsed()) {
          if (this.departingPlanesList.isEmpty() && this.landingPlanesList.isEmpty())
            beginRunwayMaintenance(runway, rc);
        } else if (rc.scheduler.shouldBeAnnouncedNow()) {
          announceScheduledRunwayCheck(runway, rc);
        }
      }
    }
  }

  private void beginRunwayMaintenance(Runway runway, RunwayCheck rc) {
    StringResponse cnt = StringResponse.create(
        "Maintenance of the runway %s is now in progress.", runway.getName());
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
      cnt = StringResponse.create("Runway %s is under maintenance right now until approximately %d:%02d.",
          rwy.getName(),
          rc.realDurationEnd.getHours(), rc.realDurationEnd.getMinutes()
      );
    else {
      cnt = StringResponse.create("Runway %s maintenance is scheduled at %s for approximately %d minutes.",
          rwy.getName(),
          rc.scheduler.getScheduledTime().toTimeString(),
          rc.expectedDurationInMinutes);
      rc.scheduler.nowAnnounced();
    }

    Message msg = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(msg);

  }

  private void checkForRunwayChange() {
    List<RunwayThreshold> newSuggested = getSuggestedThresholds();

    boolean isSame = CollectionUtils.containsSameItems(newSuggested, inUseInfo.current);
    if (!isSame){
      inUseInfo.scheduler = new SchedulerForAdvice(Acc.now().addSeconds(10*60));
      inUseInfo.scheduled = newSuggested;
    }
  }

  private void changeRunwayInUse() {

    EStringBuilder str = new EStringBuilder();
    str.appendLine("Changed runway(s) in use now to: ");
    str.appendItems(inUseInfo.scheduled, q -> q.getName(), ", ");
    str.append(".");

    Message m = new Message(
        this,
        Acc.atcApp(),
        StringResponse.create(str.toString()));
    super.sendMessage(m);
    this.inUseInfo.current = this.inUseInfo.scheduled;
    this.inUseInfo.scheduled = null;
    this.inUseInfo.scheduler = null;

    for (RunwayThreshold threshold : this.inUseInfo.current) {
      Runway rwy = threshold.getParent();
      announceScheduledRunwayCheck(rwy,
          this.runwayChecks.get(rwy));
    }
  }

  private void tryToLog(String format, Object... params) {
    if (toRecorder != null)
      toRecorder.write(format, params);
  }

  private void tryTakeOffPlane() {
    boolean isAnyAvailable = false;
    for (RunwayThreshold threshold : inUseInfo.current) {
      RunwayCheck rc = runwayChecks.get(threshold.getParent());
      if (rc.isActive() == false) {
        isAnyAvailable = true;
        break;
      }
    }
    if (isAnyAvailable == false) return;

    // tryToLog("tryTakeOffPlane");
    if (linedUpPlanesList.isEmpty()) {
      // tryToLog("lineUp list empty");
      return;
    }

    Airplane toReadyPlane = linedUpPlanesList.get(0);
    // tryToLog("Plane to take off: %s", toReadyPlane.getCallsign().toString());

    // tryToLog("\tChecked threshold %s", inUseInfo.current.get(0));


    if (takeOffInfos.isLatestDepartureBelow(inUseInfo.current, Acc.airport().getAltitude() + 300)) {
      return;
    }
    if (takeOffInfos.isLatestDepartureSeparated(inUseInfo.current, toReadyPlane.getType().category) == false) {
      return;
    }

    List<RunwayThreshold> availableThresholds = new ArrayList<>();
    for (RunwayThreshold threshold : inUseInfo.current) {
      double closestLandingPlaneDistance = closestLandingPlaneDistance(threshold);
      if (closestLandingPlaneDistance > 2.5 && this.runwayChecks.get(threshold.getParent()).isActive() == false) {
        availableThresholds.add(threshold);
      }
    }
    RunwayThreshold availableThreshold;
    if (availableThresholds.isEmpty()) {
      return;
    } else {
      availableThreshold = CollectionUtils.getRandom(availableThresholds);
    }


    // if it gets here, the "toReadyPlane" can proceed take-off
    linedUpPlanesList.remove(0);
    departingPlanesList.add(toReadyPlane);
    toReadyPlane.setHoldingPointState(availableThreshold.getCoordinate(), availableThreshold.getCourse());

    // add to stats
    double diffSecs = ETime.getDifference(Acc.now(), this.holdingPointWaitingTimeMap.get(toReadyPlane)).getTotalSeconds();
    diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
    if (diffSecs < 0) diffSecs = 0;
    Acc.stats().holdingPointInfo.maximumHoldingPointTime.set(diffSecs);
    Acc.stats().holdingPointInfo.meanHoldingPointTime.add(diffSecs);

    // process the T-O
    TakeOffInfo toi = new TakeOffInfo(
        Acc.now(), toReadyPlane);
    this.takeOffInfos.set(availableThreshold, toi);

    SpeechList lst = new SpeechList();
    lst.add(new RadarContactConfirmationNotification());
    lst.add(new ClearedForTakeoffCommand(availableThreshold));

    // TO altitude only when no altitude from SID already processed
    if (toReadyPlane.getTargetAltitude() <= availableThreshold.getParent().getParent().getAltitude())
      lst.add(new ChangeAltitudeCommand(
          ChangeAltitudeCommand.eDirection.climb, availableThreshold.getInitialDepartureAltitude()));

    // -- po vysce+300 ma kontaktovat APP
    lst.add(new AfterAltitudeCommand(
        Acc.airport().getAltitude() + Acc.rnd().nextInt(150, 450),
        AfterAltitudeCommand.ERestriction.andAbove));
    lst.add(new ContactCommand(Atc.eType.app));

    Message m = new Message(this, toReadyPlane, lst);
    super.sendMessage(m);
  }

  private double closestLandingPlaneDistance(RunwayThreshold threshold) {
    double ret = Double.MAX_VALUE;
    for (Airplane plane : Acc.planes()) {
      if (plane.getState().is(
          Airplane.State.landed,
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

}

class TakeOffInfo {
  public final ETime takeOffTime;
  public final Airplane airplane;
  public final int randomReadyToSwitchAltitude;

  public TakeOffInfo(ETime takeOffTime, Airplane airplane) {
    this.takeOffTime = takeOffTime.clone();
    this.airplane = airplane;
    this.randomReadyToSwitchAltitude = Acc.airport().getAltitude() + Acc.rnd().nextInt(250, 1000);
  }
}

class TakeOffInfos extends EMap<RunwayThreshold, TakeOffInfo> {

  private final static int[][] sepDistanceNm = new int[][]{
      new int[]{0, 0, 0, 0}, // A
      new int[]{4, 3, 0, 0}, // B
      new int[]{5, 3, 3, 0}, // C
      new int[]{6, 6, 5, 4}}; // D
  private final static int[][] sepTimeSeconds = new int[][]{
      new int[]{120, 0, 0, 0}, // A
      new int[]{120, 120, 0, 0}, // B
      new int[]{120, 120, 120, 0}, // C
      new int[]{180, 180, 180, 120}}; // D

  private static int c2i(char c) {
    switch (c) {
      case 'A':
        return 0;
      case 'B':
        return 1;
      case 'C':
        return 2;
      case 'D':
        return 3;
      default:
        throw new ENotSupportedException("Unknown plane type category " + c);
    }
  }

  public boolean isLatestDepartureBelow(List<RunwayThreshold> checkedThresholds, double altitudeInFt) {
    boolean ret = false;

    for (RunwayThreshold threshold : checkedThresholds) {
      TakeOffInfo toi = this.get(threshold);
      if (toi == null) continue; // no mapping for threshold yet
      if (toi.airplane.getAltitude() < altitudeInFt) {
        ret = true;
        break;
      }
    }

    return ret;
  }

  public boolean isLatestDepartureSeparated(List<RunwayThreshold> current, char planeCategory) {
    boolean ret = true;
    for (RunwayThreshold threshold : current) {
      TakeOffInfo toi = this.get(threshold);
      if (toi == null) continue;
      int[] separationIndices = getSeparationIndices(toi.airplane.getType().category, planeCategory);

      if (!isTimeSeparated(separationIndices, toi) ||
          !isDistanceSeparated(separationIndices, toi, threshold)) {
        ret = false;
        break;
      }
    }

    return ret;
  }

  private boolean isDistanceSeparated(int[] separationIndices, TakeOffInfo toi, RunwayThreshold threshold) {
    int minDistance = sepDistanceNm[separationIndices[0]][separationIndices[1]];
    double curDist = Coordinates.getDistanceInNM(toi.airplane.getCoordinate(), threshold.getCoordinate());
    boolean ret = curDist > minDistance;
    return ret;
  }

  private boolean isTimeSeparated(int[] separationIndices, TakeOffInfo toi) {
    int minSeconds = sepTimeSeconds[separationIndices[0]][separationIndices[1]];
    ETime minTime = toi.takeOffTime.addSeconds(minSeconds);
    boolean ret = minTime.isBeforeOrEq(Acc.now());
    return ret;
  }

  private int[] getSeparationIndices(char firstCategory, char secondCategory) {
    int[] ret = new int[2];
    ret[0] = c2i(firstCategory);
    ret[1] = c2i(secondCategory);
    return ret;
  }
}