package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.HashMap;
import java.util.Map;

public class TowerAtc extends ComputerAtc {

  private static final int RUNWAY_CHANGE_INFO_UPDATE_INTERVAL = 10 * 60;
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;
  private final Map<RunwayThreshold, TakeOffInfo> takeOffInfos = new HashMap<>();
  private AirplaneList landingPlanes = new AirplaneList();
  private AirplaneList goAroundedPlanesToSwitch = new AirplaneList();
  private AirplaneList holdingPointPlanesList = new AirplaneList();
  private AirplaneList linedUpPlanesList = new AirplaneList();
  private AirplaneList departingPlanesList = new AirplaneList();

  private Map<Airplane, ETime> holdingPointWaitingTimeMap = new HashMap<>();
  private RunwayThreshold runwayThresholdInUse = null;

  public TowerAtc(AtcTemplate template) {
    super(template);
  }

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

  @Override
  public void unregisterPlaneUnderControl(Airplane plane) {
    if (landingPlanes.contains(plane))
      landingPlanes.remove(plane);
    if (goAroundedPlanesToSwitch.contains(plane))
      goAroundedPlanesToSwitch.remove(plane);
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
  public void registerNewPlaneUnderControl(Airplane plane) {
    if (plane.isArrival()) {
      landingPlanes.add(plane);
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
  protected boolean canIAcceptPlane(Airplane p) {
    if (p.isDeparture()) {
      return false;
    }
    if (Acc.prm().getResponsibleAtc(p) != Acc.atcApp()) {
      return false;
    }
    if (p.getAltitude() > this.acceptAltitude) {
      return false;
    }
    double dist = Coordinates.getDistanceInNM(p.getCoordinate(), Acc.airport().getLocation());
    if (dist > MAXIMAL_ACCEPT_DISTANCE_IN_NM) {
      return false;
    }

    return true;
  }

  @Override
  protected void processMessagesFromPlane(Airplane plane, SpeechList spchs) {
    if (spchs.containsType(GoingAroundNotification.class)) {
      landingPlanes.remove(plane);
      goAroundedPlanesToSwitch.add(plane);
    }
  }

  @Override
  protected Atc getTargetAtcIfPlaneIsReadyToSwitch(Airplane plane) {
    Atc ret = null;
    if (this.goAroundedPlanesToSwitch.contains(plane)) {
      this.goAroundedPlanesToSwitch.remove(plane);
      ret = Acc.atcApp();
    } else if (plane.isDeparture()) {
      ret = Acc.atcApp();
    }
    return ret;
  }

  @Override
  public void init() {
    super.init();
    RunwayThreshold suggestedThreshold = getSuggestedThreshold();
    changeRunwayInUse(suggestedThreshold);
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return holdingPointPlanesList.size() + linedUpPlanesList.size();
  }

  public RunwayThreshold getRunwayThresholdInUse() {
    return runwayThresholdInUse;
  }

  private void checkForRunwayChange() {
  }

  private void changeRunwayInUse(RunwayThreshold newRunwayInUseThreshold) {
    Message m = new Message(
        this,
        Acc.atcApp(),
        new StringMessageContent("Runway in use %s from now.", newRunwayInUseThreshold.getName()));
    super.sendMessage(m);
    this.runwayThresholdInUse = newRunwayInUseThreshold;
  }

  private void tryTakeOffPlane() {
    if (linedUpPlanesList.isEmpty())
      return;

    Airplane toReadyPlane = linedUpPlanesList.get(0);

    TakeOffInfo toi = null;
    if (takeOffInfos.containsKey(this.runwayThresholdInUse)) {
      toi = takeOffInfos.get(this.runwayThresholdInUse);
    }

    if (toi != null) {
      if (toi.airplane.getAltitude() + 300 < Acc.airport().getAltitude())
        return;

      if (toi.separation == null) {
        toi.separation = TakeOffSeparation.create(toi.airplane.getType().category, toReadyPlane.getType().category);
      }

      if (toi.takeOffTime.addSeconds(toi.separation.seconds).isAfter(Acc.now()))
        return;

      double distance = Coordinates.getDistanceInNM(toi.airplane.getCoordinate(), Acc.threshold().getCoordinate());
      if (distance < toi.separation.nm)
        return;
    }

    if (closestLandingPlaneDistance() < 2.5)
      return;

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
      new int[]{4, 5, 6, 6}, // A
      new int[]{0, 3, 3, 5}, // B
      new int[]{0, 0, 3, 4}, // C
      new int[]{0, 0, 0, 0}}; // D
  private final static int[][] sepTimeSeconds = new int[][]{
      new int[]{120, 180, 180, 180}, // A
      new int[]{0, 120, 120, 120}, // B
      new int[]{0, 0, 120, 120}, // C
      new int[]{0, 0, 0, 120}}; // D
  /**
   * Increase frequency of arrivals. Higher means higher frequency of take-offs.
   */
  private final static double DEPARTURE_ACCELERATOR_DIVIDER = 1.3;
  public final int nm;
  public final int seconds;

  private TakeOffSeparation(int nm, int seconds) {
    this.nm = nm;
    this.seconds = seconds;
  }

  public static TakeOffSeparation create(char leadingTypeCategory, char followingTypeCategory) {
    int a = c2i(leadingTypeCategory);
    int b = c2i(followingTypeCategory);
    int time = sepTimeSeconds[a][b];
    int dist = sepDistanceNm[a][b];
    time = (int) (time * DEPARTURE_ACCELERATOR_DIVIDER);
    dist = (int) (dist * DEPARTURE_ACCELERATOR_DIVIDER);

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
}