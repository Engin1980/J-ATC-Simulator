package eng.jAtcSim.newLib.atcs.internal.tower;


import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.IAtcSwitchManagerInterface;
import eng.jAtcSim.newLib.atcs.internal.computer.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.computer.RequestResult;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.*;
import eng.jAtcSim.newLib.speeches.atc.atc2user.*;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayInUseRequest;
import eng.jAtcSim.newLib.speeches.atc.user2atc.RunwayMaintenanceRequest;
import eng.jAtcSim.newLib.weather.Weather;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.XContext;
import exml.annotations.XIgnored;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TowerAtc extends ComputerAtc {

  public enum eDirection {
    departures,
    arrivals
  }

  private class SwitchManagerInterface implements IAtcSwitchManagerInterface {

    @Override
    public boolean acceptsNewRouting(IAirplane plane, PlaneSwitchRequestRouting routing) {
      assert plane.isDeparture() : "It is nonsense to have this call here for arrival.";

      boolean ret;
      RunwayConfiguration rc;
      if (inUseInfo.getScheduled() != null && inUseInfo.scheduler.getSecondsLeft() < 300) {
        rc = inUseInfo.getScheduled();
      } else {
        rc = inUseInfo.getCurrent();
      }

      throw new ToDoException("Tady se to musi vybirat jen z aktualnich ranveji, ne z budoucich");

//    ActiveRunwayThreshold threshold = Context.getArea().getAirport().getAllThresholds().getFirst(q -> q.getName().equals(routing.getRunwayThresholdName()));
//    DARoute daRoute = Context.getArea().getAirport().getDaRoutes().getFirst(q -> q.getName().equals(routing.getRouteName()));
//    ret = threshold.getRoutes()
//            .isAny(q -> (q == daRoute || daRoute.getType() == DARouteType.vectoring)
//                    && daRoute.isValidForCategory(plane.getType().category)
//                    && daRoute.getMaxMrvaAltitude() <= plane.getType().maxAltitude
//                    && q.getMainNavaid().equals(plane.getRouting().getEntryExitPoint()));
//    return ret;
    }

    @Override
    public RequestResult canIAcceptPlaneIncomingFromAnotherAtc(IAirplane plane) {
      RequestResult ret;
      if (plane.isDeparture()) {
        ret = new RequestResult(false, String.format("%s is a departure.", plane.getCallsign()));
      } else {
        if (AirplaneState.approachDescend.is(AirplaneState.approachEntry, AirplaneState.approachDescend, AirplaneState.longFinal, AirplaneState.shortFinal) == false)
          ret = new RequestResult(false, String.format("%s is not cleared to approach.", plane.getCallsign()));
        if (isOnApproachOfTheRunwayInUse(plane) == false)
          ret = new RequestResult(false, String.format("%s is cleared to approach on the inactive runway.", plane.getCallsign()));
        else if (isRunwayThresholdUnderMaintenance(plane.getRouting().getAssignedRunwayThreshold()) == false) {
          ret = new RequestResult(false, String.format("Runway %s is closed now.", plane.getRouting().getAssignedRunwayThreshold().getParent().getName()));
        } else if (plane.getSha().getAltitude() > TowerAtc.this.getAcceptAltitude()) {
          ret = new RequestResult(false, String.format("%s is too high.", plane.getCallsign()));
        } else {
          double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), Context.getArea().getAirport().getLocation());
          if (dist > MAXIMAL_ACCEPT_DISTANCE_IN_NM) {
            ret = new RequestResult(false, String.format("%s is too far.", plane.getCallsign()));
          } else
            ret = new RequestResult(true, null);
        }
      }

      return ret;
    }

    @Override
    public AtcId getAtcId() {
      return TowerAtc.this.getAtcId();
    }

    @Override
    public AtcId getAtcIdWhereIAmSwitchingPlanes() {
      return Context.Internal.getApp().getAtcId();
    }

    @Override
    public IReadOnlyList<IAirplane> getPlanesUnderControl() {
      IList<IAirplane> ret = TowerAtc.this.departureManager.getAllPlanes().union(TowerAtc.this.arrivalManager.getAllPlanes());
      return ret;
    }

    @Override
    public boolean isPlaneReadyToSwitchToAnotherAtc(IAirplane plane) {
      boolean ret;
      if (plane.isArrival()) {
        ret = TowerAtc.this.arrivalManager.checkIfPlaneIsReadyToSwitchAndRemoveIt(plane);
      } else if (plane.isDeparture()) {
        ret = TowerAtc.this.departureManager.isPlaneReadyToSwitch(plane);
      } else
        ret = false;
      return ret;
    }

    @Override
    public void onAfterIncomingPlaneGoodDayNotificationConfirmed(Squawk sqwk) {
      IAirplane plane = Context.Internal.getPlane(sqwk);
      TowerAtc.this.arrivalManager.registerNewArrival(plane);
    }

    @Override
    public void onOutgoingPlaneSwitchCompleted(Squawk squawk) {
      IAirplane plane = Context.Internal.getPlane(squawk);
      if (plane.isDeparture()) {
        departureManager.confirmedByApproach(plane);
      } else {
        arrivalManager.goAroundPlane(plane);
      }
    }

    @Override
    public void sendMessage(Message msg) {
      TowerAtc.this.sendMessage(msg);
    }
  }

  private static final int[] RWY_CHANGE_ANNOUNCE_INTERVALS = new int[]{30, 15, 10, 5, 4, 3, 2, 1};
  private static final int MAXIMAL_SPEED_FOR_PREFERRED_RUNWAY = 5;
  private static final double MAXIMAL_ACCEPT_DISTANCE_IN_NM = 15;

  private static RunwayConfiguration getSuggestedThresholds() {
    RunwayConfiguration ret = null;
    Weather w = Context.getWeather().getWeather();

    for (RunwayConfiguration rc : Context.getArea().getAirport().getRunwayConfigurations()) {
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
    Weather w = Context.getWeather().getWeather();
    Airport airport = Context.getArea().getAirport();
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

  @XIgnored
  private final SwitchManagerInterface switchManagerInterface = new SwitchManagerInterface();
  private final DepartureManager departureManager;
  private final ArrivalManager arrivalManager;
  private RunwaysInUseInfo inUseInfo = null;
  private EMap<String, RunwayCheckInfo> runwayChecks = null;
  private boolean isUpdatedWeather;

  public TowerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
    this.departureManager = new DepartureManager();
    this.arrivalManager = new ArrivalManager();
  }

  @XmlConstructor
  private TowerAtc() {
    this.departureManager = null;
    this.arrivalManager = null;
  }

  public int getNumberOfPlanesAtHoldingPoint() {
    return departureManager.getNumberOfPlanesAtHoldingPoint();
  }

  public RunwayConfiguration getRunwayConfigurationInUse() {
    return inUseInfo.current;
  }

  @Override
  public void init() {
    super.init();

    departureManager.bind(this, m -> this.sendMessage(m));
    arrivalManager.bind(this);

    if (runwayChecks == null) { // loading if not null
      runwayChecks = new EMap<>();
      for (ActiveRunway runway : Context.getArea().getAirport().getRunways()) {
        RunwayCheckInfo rc = RunwayCheckInfo.createNormal(true);
        runwayChecks.set(runway.getName(), rc);
      }
    }

    if (inUseInfo == null) {
      // loading otherwise
      inUseInfo = new RunwaysInUseInfo();
      inUseInfo.current = getSuggestedThresholds();
      inUseInfo.scheduler = null;
      Context.getArea().setCurrentRunwayConfiguration(this.inUseInfo.current);
      Context.getArea().setScheduledRunwayConfiguration(null);
    }

    Context.getWeather().onWeatherUpdated().add(() -> this.isUpdatedWeather = true);
  }

  public boolean isRunwayThresholdUnderMaintenance(ActiveRunwayThreshold threshold) {
    boolean ret = runwayChecks.get(threshold.getParent().getName()).isActive() == false;
    return ret;
  }

  @Override
  public void registerNewPlaneInGame(Callsign callsign, boolean initialRegistration) {
    IAirplane plane = Context.Internal.getPlane(callsign);
    if (plane.isArrival())
      arrivalManager.registerNewArrival(plane);
    else {
      ActiveRunwayThreshold runwayThreshold = getRunwayThresholdForDeparture(plane);
      departureManager.registerNewDeparture(plane, runwayThreshold);
    }
  }

  public RunwayConfiguration tryGetRunwayConfigurationScheduled() {
    return inUseInfo.scheduled;
  }

  @Override
  public void unregisterPlaneDeletedFromGame(Callsign callsign, boolean isForcedDeletion) {
    IAirplane plane = Context.Internal.getPlane(callsign);
    if (plane.isDeparture()) {
      if (isForcedDeletion == false) {
        // add to stats
        EDayTimeStamp holdingPointEntryTime = departureManager.getAndEraseHoldingPointEntryTime(plane);
        int diffSecs = Context.getShared().getNow().getValue() - holdingPointEntryTime.getValue();
        diffSecs -= 15; // generally let TWR atc asks APP atc to switch 15 seconds before HP.
        if (diffSecs < 0) diffSecs = 0;
        //TODO this stats value should be increased outside of Tower atc??
        Context.getStats().getStatsProvider().registerDeparture(diffSecs);
        departureManager.deletePlane(plane);
      }
    } else { // plane.isArrival()
      arrivalManager.deletePlane(plane);
      //TODO this will add to stats even planes deleted from the game by a user(?)
      //TODO this stats value should be increased outside of Tower atc??
      Context.getStats().getStatsProvider().registerArrival();
    }

    if (!isForcedDeletion && plane.isEmergency() && plane.getState() == AirplaneState.landed) {
      // if it is landed emergency, close runway for amount of time
      ActiveRunway rwy = plane.getRouting().getAssignedRunwayThreshold().getParent();
      RunwayCheckInfo rwyCheck = RunwayCheckInfo.createImmediateAfterEmergency();
      runwayChecks.set(rwy.getName(), rwyCheck);
    }
  }

  @Override
  protected IAtcSwitchManagerInterface getSwitchManagerInterface() {
    return this.switchManagerInterface;
  }

  @Override
  protected void _elapseSecond() {
    //TODO here should be some check that landing plane is not landing on the occupied runway
    this.departureManager.movePlanesToHoldingPoint();
    this.tryTakeOffPlaneNew();
    this.hangOffDepartedPlanes();
    this.processRunwayCheckBackground();
    this.processRunwayChangeBackground();
  }

  private void hangOffDepartedPlanes() {
    IReadOnlyList<IAirplane> departedPlanes = this.departureManager.getDepartedPlanesReadyToHangoff(true);
    for (IAirplane plane : departedPlanes) {
      Message msg = new Message(
              Participant.createAtc(this.getAtcId()),
              Participant.createAirplane(plane.getCallsign()),
              new SpeechList<>(new ContactCommand(this.switchManagerInterface.getAtcIdWhereIAmSwitchingPlanes())));
      super.sendMessage(msg);
    }
  }

  private void announceChangeRunwayInUse() {
    sendMessageToUser(
            new RunwayInUseNotification(
                    this.inUseInfo.scheduled.getDepartures().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(q.getThreshold().getName(), q.getCategories())),
                    this.inUseInfo.scheduled.getArrivals().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(q.getThreshold().getName(), q.getCategories())),
                    this.inUseInfo.scheduler.getScheduledTime()
            )
    );
  }

  private void announceScheduledRunwayCheck(String runwayName, RunwayCheckInfo rc) {
    RunwayMaintenanceBaseNotification cnt;
    if (rc.isActive())
      cnt = new RunwayMaintenanceProceedingNotification(runwayName, rc.getRealDurationEnd());
    else {
      cnt = new RunwayMaintenanceScheduledNotification(
              runwayName, rc.getScheduler().getScheduledTime(), rc.getExpectedDurationInMinutes());
      rc.getScheduler().nowAnnounced();
    }

    Message msg = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(Context.Internal.getApp().getAtcId()),
            cnt);
    super.sendMessage(msg);

  }

  private void beginRunwayMaintenance(String rwyName, RunwayCheckInfo rc) {
    RunwayMaintenanceProceedingNotification cnt = new RunwayMaintenanceProceedingNotification(
            rwyName, Context.getShared().getNow().addMinutes(rc.getExpectedDurationInMinutes()));
    Message m = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(Context.Internal.getApp().getAtcId()),
            cnt);
    super.sendMessage(m);

    rc.start();
  }

  private void changeRunwayInUse() {
    this.inUseInfo.current = this.inUseInfo.scheduled;
    this.inUseInfo.scheduled = null;
    this.inUseInfo.scheduler = null;

    sendMessageToUser(new RunwayInUseNotification(
            inUseInfo.current.getDepartures().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                    q.getThreshold().getName(),
                    q.getCategories())),
            inUseInfo.current.getArrivals().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                    q.getThreshold().getName(),
                    q.getCategories())),
            null));


    IList<ActiveRunway> tmp =
            this.inUseInfo.current.getDepartures()
                    .select(q -> q.getThreshold().getParent())
                    .union(this.inUseInfo.current.getArrivals().select(q -> q.getThreshold().getParent()));
    tmp.forEach(q -> announceScheduledRunwayCheck(q.getName(), this.runwayChecks.get(q.getName())));

    Context.getArea().setCurrentRunwayConfiguration(this.inUseInfo.scheduled);
    Context.getArea().setScheduledRunwayConfiguration(null);
  }

  private void checkForRunwayChange() {
    RunwayConfiguration newSuggested = getSuggestedThresholds();

    boolean isSame = inUseInfo.current.isUsingTheSameRunwayConfiguration(newSuggested);
    if (!isSame) {
      inUseInfo.scheduler = new SchedulerForAdvice(Context.getShared().getNow().addSeconds(10 * 60), RWY_CHANGE_ANNOUNCE_INTERVALS);
      inUseInfo.scheduled = newSuggested;
    }
  }

  private void finishRunwayMaintenance(String rwyName, RunwayCheckInfo rc) {
    RunwayMaintenanceEndedNotification cnt = new RunwayMaintenanceEndedNotification(rwyName);
    Message m = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(Context.Internal.getApp().getAtcId()),
            cnt);
    super.sendMessage(m);

    rc = RunwayCheckInfo.createNormal(false);
    runwayChecks.set(rwyName, rc);
  }

  private double getDepartingPlaneSwitchAltitude(char category) {
    switch (category) {
      case 'A':
        return (double) Context.getArea().getAirport().getAltitude() + Context.getApp().getRnd().nextInt(100, 250);
      case 'B':
        return (double) Context.getArea().getAirport().getAltitude() + Context.getApp().getRnd().nextInt(150, 400);
      case 'C':
      case 'D':
        return (double) Context.getArea().getAirport().getAltitude() + Context.getApp().getRnd().nextInt(200, 750);
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
    restrictToRunwaysNotUnderLongMaintenance(rts);
    if (rts.size() > 0)
      ret = rts.getRandom();
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
    ret = dist < 2.5;
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
    boolean ret = departureManager.getLastDepartureTime(runwayThreshold).addSeconds(60).isAfterOrEq(Context.getShared().getNow());
    return ret;
  }

  private boolean isRunwayUnderMaintenance(ActiveRunway runway) {
    return this.runwayChecks.get(runway.getName()).isActive();
  }

  private void processMessageFromAtc(RunwayInUseRequest ru) {

    if (ru.getType() == RunwayInUseRequest.eType.changeNowRequest) {
      if (inUseInfo.scheduled == null) {
        Message msg = new Message(
                Participant.createAtc(this.getAtcId()),
                Participant.createAtc(Context.Internal.getApp().getAtcId()),
                new AtcRejection(ru, "There is no scheduled runway change."));
        super.sendMessage(msg);
      } else {
        // force runway change
        changeRunwayInUse();
      }
    } else {
      sendMessageToUser(new RunwayInUseNotification(
              inUseInfo.current.getDepartures().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                      q.getThreshold().getName(),
                      q.getCategories())),
              inUseInfo.current.getArrivals().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                      q.getThreshold().getName(),
                      q.getCategories())),
              null));

      if (inUseInfo.scheduled != null) {
        sendMessageToUser(new RunwayInUseNotification(
                inUseInfo.current.getDepartures().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                        q.getThreshold().getName(),
                        q.getCategories())),
                inUseInfo.current.getArrivals().select(q -> new RunwayInUseNotification.RunwayThresholdInUseInfo(
                        q.getThreshold().getName(),
                        q.getCategories())),
                inUseInfo.scheduler.getScheduledTime()));
      }
    }
  }

  private void processMessageFromAtc(RunwayMaintenanceRequest rrct) {
    if (rrct.type == RunwayMaintenanceRequest.eType.askForTime) {
      RunwayCheckInfo rc = this.runwayChecks.tryGet(rrct.runway);
      if (rc != null)
        announceScheduledRunwayCheck(rrct.runway, rc);
      else {
        for (String runwayName : this.runwayChecks.getKeys()) {
          rc = this.runwayChecks.get(runwayName);
          announceScheduledRunwayCheck(runwayName, rc);
        }
      }
    } else if (rrct.type == RunwayMaintenanceRequest.eType.doCheck) {
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
                Participant.createAtc(Context.Internal.getApp().getAtcId()),
                new AtcRejection(rrct, "Sorry, you must specify exact runway (threshold) at which I can start the maintenance."));
        super.sendMessage(msg);
      } else {
        if (rc.isActive()) {
          Message msg = new Message(
                  Participant.createAtc(this.getAtcId()),
                  Participant.createAtc(Context.Internal.getApp().getAtcId()),
                  new AtcRejection(rrct, sf("The runway %s is already under maintenance right now.", rwyName)));
          super.sendMessage(msg);
        } else if (rc.getScheduler().getMinutesLeft() > 30) {
          Message msg = new Message(
                  Participant.createAtc(this.getAtcId()),
                  Participant.createAtc(Context.Internal.getApp().getAtcId()),
                  new AtcRejection(rrct, sf("Sorry, the runway %s is scheduled for the maintenance in more than 30 minutes.",
                          rwyName)));
          super.sendMessage(msg);
        } else {
          Message msg = new Message(
                  Participant.createAtc(this.getAtcId()),
                  Participant.createAtc(Context.Internal.getApp().getAtcId()),
                  new AtcConfirmation(rrct));
          super.sendMessage(msg);
          rc.getScheduler().setApprovedTrue();
        }
      }
    }
  }

  @Override
  protected void processMessagesFromPlaneExceptGoodDayNotification(IAirplane plane, SpeechList<IFromPlaneSpeech> spchs) {
    if (spchs.containsType(GoingAroundNotification.class)) {
      arrivalManager.goAroundPlane(plane);
    }
  }

  @Override
  protected void processNonPlaneSwitchMessageFromAtc(Message m) {
    if (m.getContent() instanceof RunwayCheckInfo) {
      RunwayMaintenanceRequest rrct = m.getContent();
      processMessageFromAtc(rrct);
    } else if (m.getContent() instanceof RunwayInUseRequest) {
      RunwayInUseRequest ru = m.getContent();
      processMessageFromAtc(ru);
    }
  }

  private void processRunwayChangeBackground() {
    if (inUseInfo.scheduler == null) {
      if (isUpdatedWeather) {
        if (Context.getWeather().getWeather().getSnowState() != Weather.eSnowState.none)
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
        if (rc.getRealDurationEnd().isBeforeOrEq(Context.getShared().getNow()))
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

  private void restrictToRunwaysNotUnderLongMaintenance(IList<ActiveRunwayThreshold> rts) {
    rts.remove(q -> {
      ActiveRunway r = q.getParent();
      RunwayCheckInfo rt = runwayChecks.get(r.getName());
      boolean ret = rt.isActive() && rt.getExpectedDurationInMinutes() > 5;
      return ret;
    });
  }

  private void sendMessageToUser(IMessageContent content) {
    Message msg = new Message(
            Participant.createAtc(this.getAtcId()),
            Participant.createAtc(Context.Internal.getApp().getAtcId()),
            content);
    super.sendMessage(msg);
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

    SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
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
    Weather weather = Context.getWeather().getWeather();
    for (String rwyName : this.runwayChecks.getKeys()) {
      RunwayCheckInfo rc = this.runwayChecks.get(rwyName);
      if (rc.isActive()) continue;
      int maxInterval = weather.getSnowState() == Weather.eSnowState.intensive
              ? RunwayCheckInfo.MAX_SNOW_INTENSIVE_MAINTENANCE_INTERVAL
              : RunwayCheckInfo.MAX_SNOW_MAINTENANCE_INTERVAL;
      if (rc.getScheduler().getMinutesLeft() > maxInterval) {
        rc = RunwayCheckInfo.createSnowCleaning(
                false,
                weather.getSnowState() == Weather.eSnowState.intensive);
        runwayChecks.set(rwyName, rc);
        announceScheduledRunwayCheck(rwyName, rc);
      }
    }
  }
}
