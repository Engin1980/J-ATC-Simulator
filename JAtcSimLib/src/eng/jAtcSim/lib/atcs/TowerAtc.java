package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.StringResponse;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.HashMap;
import java.util.Map;

public class TowerAtc extends ComputerAtc {

  public static class RunwayCheck {
    private ETime latestScheduledTime;
    private int expectedDurationInMinutes;
    private int lastAnnouncedSecond;
    private boolean isActive;
    private ETime realDurationEnd;
    private boolean approvedByAppAtc;


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

    public RunwayCheck(ETime latestScheduledTime, int expectedDurationInMinutes) {
      this.latestScheduledTime = latestScheduledTime;
      this.lastAnnouncedSecond = Integer.MAX_VALUE;
      this.expectedDurationInMinutes = expectedDurationInMinutes;
    }

    public RunwayCheck(int minutesToNextCheck, int expectedDurationInMinutes) {
      ETime et = Acc.now().addMinutes(minutesToNextCheck);
      this.latestScheduledTime = et;
      this.expectedDurationInMinutes = expectedDurationInMinutes;
      this.lastAnnouncedSecond = Integer.MAX_VALUE;
    }

    public int getSecondsLeft() {
      int diff = latestScheduledTime.getTotalSeconds() - Acc.now().getTotalSeconds();
      return diff;
    }

    public void start() {
      double durationRangeSeconds = expectedDurationInMinutes * 60 * 0.2d;
      int realDurationSeconds = (int) Acc.rnd().nextDouble(
          expectedDurationInMinutes * 60 - durationRangeSeconds,
          expectedDurationInMinutes * 60 + durationRangeSeconds);
      realDurationEnd = Acc.now().addSeconds(realDurationSeconds);
      isActive = true;
      latestScheduledTime = null;
    }
  }

  private static final int RUNWAY_CHANGE_INFO_UPDATE_INTERVAL = 10 * 60;
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;
  private final Map<RunwayThreshold, TakeOffInfo> takeOffInfos = new HashMap<>();
  private final CommonRecorder toRecorder;
  private AirplaneList landingPlanesList = new AirplaneList();
  private AirplaneList goAroundedPlanesToSwitchList = new AirplaneList();
  private AirplaneList holdingPointPlanesList = new AirplaneList();
  private AirplaneList linedUpPlanesList = new AirplaneList();
  private AirplaneList departingPlanesList = new AirplaneList();
  private Map<Airplane, ETime> holdingPointWaitingTimeMap = new HashMap<>();
  private RunwayThreshold runwayThresholdInUse = null;
  private Map<Runway, RunwayCheck> runwayChecks = null;
  private int[] runwayCheckAnnounceTimes = new int[]{30 * 60, 15 * 60, 10 * 60, 5 * 60};

  private static RunwayThreshold getSuggestedThreshold() {
    Weather w = Acc.weather();

    RunwayThreshold rt = null;

    if (w.getWindSpeetInKts() <= MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY) {
      for (Runway r : Acc.airport().getRunways()) {
        if (r.isActive() == false) {
          continue; // skip inactive runways
        }
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

    if (Acc.now().getTotalSeconds() % RUNWAY_CHANGE_INFO_UPDATE_INTERVAL == 0) {
      checkForRunwayChange();
    }

    processRunwayCheckBackground();

  }

  @Override
  protected void processMessageFromAtc(Message m) {
    if (m.getContent() instanceof eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck) {
      eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck rrct = m.getContent();
      if (rrct.type == eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck.eType.askForTime) {
        RunwayCheck rc = this.runwayChecks.get(rrct.runway);
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
        RunwayCheck rc = this.runwayChecks.get(rwy);
        if (rc == null && rrct.runway == null && this.runwayChecks.size()==1){
          rwy = runwayThresholdInUse.getParent();
          rc = this.runwayChecks.get(rwy);
        }
        if (rc == null) {
          Message msg = new Message(this, Acc.atcApp(),
              StringResponse.createRejection("Sorry, you must specify exact runway (threshold) at which I can start the maintenance."));
          super.sendMessage(msg);
        } else {
          if (rc.getSecondsLeft() > 30 * 60) {
            Message msg = new Message(this, Acc.atcApp(),
                StringResponse.createRejection("Sorry, the runway %s is scheduled for the maintenance in more than 30 minutes.",
                    rwy.getName()));
            super.sendMessage(msg);
          } else {
            Message msg = new Message(this, Acc.atcApp(),
                StringResponse.createRejection("The maintenance of the runway %s is approved and will start shortly.",
                    rwy.getName()));
            super.sendMessage(msg);
            rc.approvedByAppAtc = true;
          }
        }
      }
    }
  }

  @Override
  public void init() {
    super.init();

    runwayChecks = new HashMap<>();
    for (Runway runway : Acc.airport().getRunways()) {
      if (runway.isActive() == false) continue;
      RunwayCheck rc = TowerAtc.RunwayCheck.createNormal(true);
      runwayChecks.put(runway, rc);
    }

    RunwayThreshold suggestedThreshold = getSuggestedThreshold();
    changeRunwayInUse(suggestedThreshold);
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
    if (isActiveRunwayClosed()){
      return new RequestResult(false, "Active runway is closed now.");
    }
    if (p.isDeparture()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is a departure.", p.getCallsign()));
    }
    if (Acc.prm().getResponsibleAtc(p) != Acc.atcApp()) {
      return new ComputerAtc.RequestResult(false, String.format("%s is not from APP.", p.getCallsign()));
    }
    if (isOnApproachOfTheRunwayInUse(p) == false){
      return new ComputerAtc.RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", p.getCallsign()));
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

  private boolean isOnApproachOfTheRunwayInUse(Airplane p) {
    boolean ret = runwayThresholdInUse == p.tryGetCurrentApproachRunwayThreshold();
    return ret;
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

  public boolean isActiveRunwayClosed() {
    RunwayCheck rc = runwayChecks.get(this.runwayThresholdInUse.getParent());
    boolean ret = rc.isActive;
    return ret;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return holdingPointPlanesList.size() + linedUpPlanesList.size();
  }

  public RunwayThreshold getRunwayThresholdInUse() {
    return runwayThresholdInUse;
  }

  private void processRunwayCheckBackground() {
    for (Runway runway : runwayChecks.keySet()) {
      RunwayCheck rc = runwayChecks.get(runway);
      if (rc.isActive) {
        if (rc.realDurationEnd.isBeforeOrEq(Acc.now()))
          finishRunwayMaintenance(runway, rc);
      } else {
        int secLeft = rc.getSecondsLeft();
        if (secLeft < 0 || (secLeft < (30 * 60) && rc.approvedByAppAtc)) {
          if (this.departingPlanesList.isEmpty() && this.landingPlanesList.isEmpty())
            beginRunwayMaintenance(runway, rc);
        }else {
          for (int dit : runwayCheckAnnounceTimes) {
            if (rc.lastAnnouncedSecond > dit && secLeft < dit) {
              announceScheduledRunwayCheck(runway, rc);
              break;
            }
          }
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
    runwayChecks.put(runway, rc);
  }

  private void announceScheduledRunwayCheck(Runway rwy, RunwayCheck rc) {
    StringResponse cnt;
    if (rc.isActive)
      cnt = StringResponse.create( "Runway %s is under maintenance right now until approximately %d:%02d.",
          rwy.getName(),
          rc.realDurationEnd.getHours(), rc.realDurationEnd.getMinutes()
      );
    else {
      cnt = StringResponse.create( "Runway %s maintenance is scheduled at %s for approximately %d minutes.",
          rwy.getName(),
          rc.latestScheduledTime.toTimeString(),
          rc.expectedDurationInMinutes);
      rc.lastAnnouncedSecond = rc.getSecondsLeft();
    }

    Message msg = new Message(this, Acc.atcApp(), cnt);
    super.sendMessage(msg);

  }

  private void checkForRunwayChange() {
  }

  private void changeRunwayInUse(RunwayThreshold newRunwayInUseThreshold) {
    Message m = new Message(
        this,
        Acc.atcApp(),
        StringResponse.create( "Runway in use %s from now.", newRunwayInUseThreshold.getName()));
    super.sendMessage(m);
    this.runwayThresholdInUse = newRunwayInUseThreshold;

    announceScheduledRunwayCheck(newRunwayInUseThreshold.getParent(),
        this.runwayChecks.get(newRunwayInUseThreshold.getParent()));
  }

  private void tryToLog(String format, Object... params) {
    if (toRecorder != null)
      toRecorder.write(format, params);
  }

  private void tryTakeOffPlane() {
    if (isActiveRunwayClosed()) return;

    tryToLog("tryTakeOffPlane");
    if (linedUpPlanesList.isEmpty()) {
      tryToLog("lineUp list empty");
      return;
    }

    Airplane toReadyPlane = linedUpPlanesList.get(0);
    tryToLog("Plane to take off: %s", toReadyPlane.getCallsign().toString());

    TakeOffInfo toi = null;
    if (takeOffInfos.containsKey(this.runwayThresholdInUse)) {
      toi = takeOffInfos.get(this.runwayThresholdInUse);
    }

    if (toi != null) {
      tryToLog("\tprevious departure: %s", toi.airplane.getCallsign().toString());

      if (toi.separation == null) {
        toi.separation = TakeOffSeparation.create(toi.airplane.getType().category, toReadyPlane.getType().category);
        tryToLog("\tnew separation created");
        tryToLog("\t\tmin %.2f nm, min %d seconds", toi.separation.nm, toi.separation.seconds);
      }

      if (toi.airplane.getAltitude() < Acc.airport().getAltitude() + 300) {
        tryToLog(
            "\tprevious departure altitude: %.0f, airport altitude: %d, less than 300, T-O denied",
            toi.airplane.getAltitude(),
            Acc.airport().getAltitude());
        return;
      }

      ETime lastDepartureSeparatedTime = toi.takeOffTime.addSeconds(toi.separation.seconds);
      tryToLog("\tlast departure separation time: %s", lastDepartureSeparatedTime.toString());
      if (lastDepartureSeparatedTime.isAfter(Acc.now())) {
        tryToLog("\ttime separation not achieved yet, T-O denied");
        return;
      }

      double distance = Coordinates.getDistanceInNM(toi.airplane.getCoordinate(), Acc.threshold().getCoordinate());
      tryToLog("\tlast departure separation distance: %.2f nm, minimal distance: %.2f", distance, toi.separation.nm);
      if (distance < toi.separation.nm) {
        tryToLog("\tdistance separation not achieved yet, T-O denied");
        return;
      }
    } else {
      tryToLog("\tno previous departure found.");
    }

    double closestLandingPlaneDistance = closestLandingPlaneDistance();
    tryToLog("\tclosest landing plane distance: %.2f nm", closestLandingPlaneDistance);
    if (closestLandingPlaneDistance < 2.5) {
      tryToLog("\tdistance from arrival not achieved, T_O denied");
      return;
    }

    tryToLog("\ttaking off %s", toReadyPlane.getCallsign().toString());

    // if it gets here, the "toReadyPlane" can proceed take-off
    linedUpPlanesList.remove(0);
    departingPlanesList.add(toReadyPlane);

    // add to stats
    double diffSecs = ETime.getDifference(Acc.now(), this.holdingPointWaitingTimeMap.get(toReadyPlane)).getTotalSeconds();
    diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
    if (diffSecs < 0) diffSecs = 0;
    Acc.stats().holdingPointInfo.maximumHoldingPointTime.set(diffSecs);
    Acc.stats().holdingPointInfo.meanHoldingPointTime.add(diffSecs);

    // process the T-O
    toi = new TakeOffInfo(
        Acc.now(), toReadyPlane);
    this.takeOffInfos.put(this.runwayThresholdInUse, toi);

    SpeechList lst = new SpeechList();
    lst.add(new RadarContactConfirmationNotification());
    lst.add(new ClearedForTakeoffCommand(runwayThresholdInUse));
    Message m = new Message(this, toReadyPlane, lst);
    super.sendMessage(m);
  }

  private double closestLandingPlaneDistance() {
    double ret = Double.MAX_VALUE;
    for (Airplane plane : Acc.planes()) {
      if (plane.getState().is(
          Airplane.State.landed,
          Airplane.State.shortFinal,
          Airplane.State.longFinal,
          Airplane.State.approachDescend
      )) {
        double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), Acc.threshold().getCoordinate());
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
  public TakeOffSeparation separation;

  public TakeOffInfo(ETime takeOffTime, Airplane airplane) {
    this.takeOffTime = takeOffTime.clone();
    this.airplane = airplane;
    this.randomReadyToSwitchAltitude = Acc.airport().getAltitude() + Acc.rnd().nextInt(250, 1000);
  }
}

class TakeOffSeparation {

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
  /**
   * Increase frequency of arrivals. Higher means higher frequency of take-offs.
   */
  private final static double DEPARTURE_ACCELERATOR_DIVIDER = 1.0;
  public final double nm;
  public final int seconds;

  public static TakeOffSeparation create(char leadingTypeCategory, char followingTypeCategory) {
    int a = c2i(leadingTypeCategory);
    int b = c2i(followingTypeCategory);
    int time = sepTimeSeconds[a][b];
    double dist = sepDistanceNm[a][b];
    time = (int) (time * DEPARTURE_ACCELERATOR_DIVIDER);
    dist = dist * DEPARTURE_ACCELERATOR_DIVIDER;

    return new TakeOffSeparation(dist, time);
  }

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

  private TakeOffSeparation(double nm, int seconds) {
    this.nm = nm;
    this.seconds = seconds;
  }
}