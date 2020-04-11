package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.IList;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.modules.*;
import eng.jAtcSim.newLib.airplanes.modules.sha.ShaModule;
import eng.jAtcSim.newLib.airplanes.other.CockpitVoiceRecorder;
import eng.jAtcSim.newLib.airplanes.other.FlightDataRecorder;
import eng.jAtcSim.newLib.airplanes.pilots.IPilotPlane;
import eng.jAtcSim.newLib.airplanes.pilots.Pilot;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedInstanceProvider;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.UnitProvider;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.ISpeech;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.weather.Weather;

public class Airplane {
//  public class Airplane4Display {
//
//    public int altitude() {
//      return Airplane.this.sha.getAltitude();
//    }
//
//    public Callsign callsign() {
//      return Airplane.this.flightModule.getCallsign();
//    }
//
//    public Coordinate coordinate() {
//      return Airplane.this.coordinate;
//    }
//
//    public Navaid entryExitPoint() {
//      return Airplane.this.routingModule.getEntryExitPoint();
//    }
//
//    public AirproxType getAirprox() {
//      return Airplane.this.mrvaAirproxModule.getAirprox();
//    }
//
//    public DARoute getAssignedRoute() {
//      return Airplane.this.routingModule.getAssignedRoute();
//    }
//
//    public ActiveRunwayThreshold getExpectedRunwayThreshold() {
//      return Airplane.this.routingModule.getAssignedRunwayThreshold();
//    }
//
//    public boolean hasRadarContact() {
//      return Airplane.this.atcModule.hasRadarContact();
//    }
//
//    public int heading() {
//      return Airplane.this.sha.getHeading();
//    }
//
//    public int ias() {
//      return Airplane.this.sha.getSpeed();
//    }
//
//    public boolean isDeparture() {
//      return Airplane.this.flightModule.isDeparture();
//    }
//
//    public boolean isEmergency() {
//      return Airplane.this.emergencyModule.isEmergency();
//    }
//
//    public boolean isMrvaError() {
//      return Airplane.this.mrvaAirproxModule.isMrvaError();
//    }
//
//    public AirplaneType planeType() {
//      return Airplane.this.airplaneType;
//    }
//
//    public Atc responsibleAtc() {
//      return Acc.prm().getResponsibleAtc(Airplane.this);
//    }
//
//    public Squawk squawk() {
//      return Airplane.this.sqwk;
//    }
//
//    public String status() {
//      Behavior behavior = Airplane.this.behaviorModule.get();
//      if (behavior instanceof BasicBehavior)
//        return behavior instanceof ArrivalBehavior ? "Arriving" : "Departing";
//      else if (behavior instanceof HoldBehavior)
//        return "Holding";
//      else if (behavior instanceof NewApproachBehavior)
//        return "In approach " + Airplane.this.routingModule.getAssignedRunwayThreshold().getName();
//      else if (behavior instanceof HoldingPointBehavior)
//        return "Holding point";
//      else if (behavior instanceof TakeOffBehavior)
//        return "Take-off";
//      else
//        return "???";
//    }
//
//    public int targetAltitude() {
//      return Airplane.this.sha.getTargetAltitude();
//    }
//
//    public int targetHeading() {
//      return Airplane.this.sha.getTargetHeading();
//    }
//
//    public int targetSpeed() {
//      return Airplane.this.sha.getTargetSpeed();
//    }
//
//    public int tas() {
//      return Airplane.this.sha.getTAS();
//    }
//
//    public Atc tunedAtc() {
//      return Airplane.this.atcModule.getTunedAtc();
//    }
//
//    public int verticalSpeed() {
//      return (int) Airplane.this.sha.getVerticalSpeed();
//    }
//  }
//
//  public class AirplaneWriteAdvanced implements IAirplaneWriteAdvanced {
//
//    @Override
//    public void abortHolding() {
//      if (Airplane.this.flightModule.isArrival())
//        Airplane.this.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
//      else
//        Airplane.this.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);
//      Airplane.this.adjustTargetSpeed();
//    }
//
//    @Override
//    public void addExperience(Mood.ArrivalExperience experience) {
//      Airplane.this.mood.experience(experience);
//    }
//
//    @Override
//    public void addExperience(Mood.DepartureExperience experience) {
//      Airplane.this.mood.experience(experience);
//    }
//
//    @Override
//    public void addExperience(Mood.SharedExperience experience) {
//      Airplane.this.mood.experience(experience);
//    }
//
//    @Override
//    public void clearedToApproach(NewApproachInfo newApproachInfo) {
//// abort holding, only if fix was found
//      if (Airplane.this.state == Airplane.State.holding) {
//        this.abortHolding();
//      }
//
//      NewApproachBehavior behavior = new NewApproachBehavior(newApproachInfo);
//      Airplane.this.setBehaviorAndState(behavior, Airplane.State.flyingIaf2Faf);
//    }
//
//    @Override
//    public void divert(boolean isInvokedByAtc) {
//      if (isInvokedByAtc) {
//        if (Airplane.this.emergencyModule.isEmergency())
//          this.addExperience(Mood.DepartureExperience.divertedAsEmergency);
//        else if (!Acc.isSomeActiveEmergency() == false)
//          this.addExperience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
//        Airplane.this.divertModule.disable();
//      } else {
//        this.addExperience(Mood.ArrivalExperience.divertOrderedByCaptain);
//      }
//
//      Navaid divertNavaid = getDivertNavaid();
//      DARoute route = DARoute.createNewVectoringByFix(divertNavaid);
//
//      Airplane.this.flightModule.divert();
//      Airplane.this.routingModule.setRoute(route);
//      Airplane.this.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);
//
//      if (!isInvokedByAtc)
//        Airplane.this.sendMessage(
//            new DivertingNotification(divertNavaid));
//    }
//
//    @Override
//    public void goAround(GoingAroundNotification.GoAroundReason gaReason) {
//      assert gaReason != null;
//
//      boolean isAtcFail = EnumUtils.is(gaReason,
//          new GoingAroundNotification.GoAroundReason[]{
//              GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
//              GoingAroundNotification.GoAroundReason.noLandingClearance,
//              GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
//              GoingAroundNotification.GoAroundReason.notStabilizedAirplane
//          });
//      if (isAtcFail)
//        this.addExperience(
//            Mood.ArrivalExperience.goAroundNotCausedByPilot);
//
//      GoingAroundNotification gan = new GoingAroundNotification(gaReason);
//      Airplane.this.sendMessage(gan);
//
//      NewApproachBehavior nab = Airplane.this.behaviorModule.getAs(NewApproachBehavior.class);
//      NewApproachInfo nai = nab.getApproachInfo();
//
//      Airplane.this.sha.setTargetSpeed(Airplane.this.airplaneType.vDep);
//      Airplane.this.sha.setTargetAltitude(Airplane.this.sha.getAltitude());
//      Airplane.this.sha.setNavigator(
//          new HeadingNavigator(nai.getRunwayThreshold().getCourse()));
//
//      SpeechList<IFromAtc> gas = new SpeechList<>(nai.getGaCommands());
//      this.prepareGoAroundRouting(gas, nai);
//      Airplane.this.routingModule.setRoute(gas);
//
//      Airplane.this.setBehaviorAndState(
//          new TakeOffBehavior(
//              Airplane.this.airplaneType.category,
//              Airplane.this.getRoutingModule().getAssignedRunwayThreshold()),
//          Airplane.State.takeOffGoAround);
//    }
//
//    @Override
//    public void hold(Navaid navaid, int inboundRadial, boolean leftTurn) {
//      HoldBehavior hold = new HoldBehavior(Airplane.this,
//          navaid,
//          inboundRadial,
//          leftTurn);
//      Airplane.this.setBehaviorAndState(hold, Airplane.State.holding);
//    }
//
//    @Override
//    public void raiseEmergency() {
//      int minsE = Acc.rnd().nextInt(5, 60);
//      double distToAip = Coordinates.getDistanceInNM(Airplane.this.coordinate, Acc.airport().getLocation());
//      int minA = (int) (distToAip / 250d * 60);
//      ETime wt = Acc.now().addMinutes(minsE + minA);
//
//      int alt = Math.max(Airplane.this.sha.getAltitude(), Acc.airport().getAltitude() + 4000);
//      alt = (int) NumberUtils.ceil(alt, 3);
//      Airplane.this.sha.setTargetAltitude(alt);
//
//      Airplane.this.emergencyModule.setEmergencyWanishTime(wt);
//      Airplane.this.flightModule.raiseEmergency();
//    }
//
//    @Override
//    public void setHoldingPointState(Coordinate coordinate, int course) {
//      Airplane.this.coordinate = coordinate;
//      Airplane.this.sha.init(course,
//          Airplane.this.sha.getAltitude(),
//          Airplane.this.sha.getSpeed(),
//          Airplane.this.airplaneType,
//          Acc.airport().getAltitude());
//      Airplane.this.state = State.holdingPoint;
//      Airplane.this.behaviorModule.setBehavior(new HoldingPointBehavior());
//    }
//
//    @Override
//    public void setRoute(SpeechList route) {
//      Airplane.this.routingModule.setRoute(route);
//    }
//
//    @Override
//    public void setRouting(DARoute route, ActiveRunwayThreshold activeRunwayThreshold) {
//      Airplane.this.routingModule.setRouting(route, activeRunwayThreshold);
//    }
//
//    @Override
//    public void takeOff(ActiveRunwayThreshold runwayThreshold) {
//      Airplane.this.coordinate = runwayThreshold.getCoordinate();
//      Airplane.this.setBehaviorAndState(
//          new TakeOffBehavior(Airplane.this.airplaneType.category, runwayThreshold),
//          Airplane.State.takeOffRoll);
//      Airplane.this.sha.setTargetSpeed(
//          Airplane.this.airplaneType.v2);
//      Airplane.this.sha.setNavigator(
//          new HeadingNavigator(runwayThreshold.getCourse()));
//    }
//
//    private Navaid getDivertNavaid() {
//      IList<DARoute> rts = Acc
//          .atcTwr().getRunwayConfigurationInUse()
//          .getDepartures()
//          .where(q -> q.isForCategory(Airplane.this.airplaneType.category))
//          .getRandom()
//          .getThreshold()
//          .getRoutes()
//          .where(q -> q.getType() == DARoute.eType.sid);
//      DARoute r = rts.getRandom();
//      //TODO here can null-pointer-exception occur when no route is found for threshold and category
//      Navaid ret = r.getMainNavaid();
//      return ret;
//    }
//
//    private boolean isBeforeRunwayThreshold(NewApproachInfo nai) {
//      double dist = Coordinates.getDistanceInNM(Airplane.this.coordinate, nai.getRunwayThreshold().getCoordinate());
//      double hdg = Coordinates.getBearing(Airplane.this.coordinate, nai.getRunwayThreshold().getCoordinate());
//      boolean ret;
//      if (dist < 3)
//        ret = false;
//      else {
//        ret = Headings.isBetween(nai.getRunwayThreshold().getCourse() - 70, hdg, nai.getRunwayThreshold().getCourse() + 70);
//      }
//      return ret;
//    }
//
//    private void prepareGoAroundRouting(SpeechList<IFromAtc> gaRoute, NewApproachInfo nai) {
//      ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
//      if (gaRoute.get(0) instanceof ChangeAltitudeCommand) {
//        cac = (ChangeAltitudeCommand) gaRoute.get(0);
//        gaRoute.removeAt(0);
//      }
//      gaRoute.insert(0, new ChangeHeadingCommand((int) nai.getRunwayThreshold().getCourse(), ChangeHeadingCommand.eDirection.any));
//
//      // check if is before runway threshold.
//      // if is far before, then first point will still be runway threshold
//      if (isBeforeRunwayThreshold(nai)) {
//        String runwayThresholdNavaidName =
//            nai.getRunwayThreshold().getParent().getParent().getIcao() + ":" + nai.getRunwayThreshold().getName();
//        Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
//        gaRoute.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
//        gaRoute.insert(1, new ThenCommand());
//      }
//
//      if (cac != null)
//        gaRoute.insert(0, cac);
//    }
//  }

  public enum State {

    /**
     * On arrival above FL100
     */
    arrivingHigh,
    /**
     * On arrival below FL100
     */
    arrivingLow,
    /**
     * On arrival < 15nm to FAF
     */
    arrivingCloseFaf,
    /**
     * When cleared to approach flying from IAF to FAF
     */
    flyingIaf2Faf,
    /**
     * Entering approach, before descend
     */
    approachEnter,
    /**
     * Descending in approach
     */
    approachDescend,
    /**
     * Long final on approach
     */
    longFinal,
    /**
     * Short final on approach
     */
    shortFinal,
    /**
     * Landed, breaking to zero
     */
    landed,

    /**
     * Waiting for take-off clearance
     */
    holdingPoint,
    /**
     * Taking off roll on the ground
     */
    takeOffRoll,
    /**
     * Take-off airborne or go-around until acceleration altitude
     */
    takeOffGoAround,
    /**
     * Departure below FL100
     */
    departingLow,
    /**
     * Departure above FL100
     */
    departingHigh,
    /**
     * In hold
     */
    holding;

    public boolean is(State... values) {
      boolean ret = false;
      for (State value : values) {
        if (this == value) {
          ret = true;
          break;
        }
      }
      return ret;
    }

    public boolean isOnGround() {
      return this == takeOffRoll || this == landed || this == holdingPoint;
    }
  }


  private static final double secondFraction = 1 / 60d / 60d;

//  public static Airplane load(XElement elm) {
//
//    throw new ToDoException();
//
//    Airplane ret = new Airplane();
//
//    LoadSave.loadField(elm, ret, "callsign");
//    LoadSave.loadField(elm, ret, "sqwk");
//    LoadSave.loadField(elm, ret, "airplaneType");
//    LoadSave.loadField(elm, ret, "delayInitialMinutes");
//    LoadSave.loadField(elm, ret, "delayExpectedTime");
//    LoadSave.loadField(elm, ret, "departure");
//    LoadSave.loadField(elm, ret, "targetHeading");
//    LoadSave.loadField(elm, ret, "targetHeadingLeftTurn");
//    LoadSave.loadField(elm, ret, "targetAltitude");
//    LoadSave.loadField(elm, ret, "targetSpeed");
//    LoadSave.loadField(elm, ret, "state");
//    LoadSave.loadField(elm, ret, "lastVerticalSpeed");
//    LoadSave.loadField(elm, ret, "airprox");
//    LoadSave.loadField(elm, ret, "mrvaError");
//    LoadSave.loadField(elm, ret, "delayResult");
//    LoadSave.loadField(elm, ret, "emergencyWanishTime");
//    LoadSave.loadField(elm, ret, "coordinate");
//    LoadSave.loadField(elm, ret, "heading");
//    LoadSave.loadField(elm, ret, "speed");
//    LoadSave.loadField(elm, ret, "altitude");
//    LoadSave.loadField(elm, ret, "mood");
//
//    ret.flightRecorder = FlightRecorder.create(ret.flightModule.getCallsign());
//
//    XElement tmp = elm.getChildren().getFirst(q -> q.getName().equals("pilot"));
//
//    ret.pilot = Pilot.load(tmp, ret.new Airplane4Pilot());
//
//    return ret;
//  }

  public static Airplane createArrival(
      Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneType,
      int heading, int altitude, int speed,
      Navaid entryPoint, int delayInitialMinutes, EDayTimeStamp delayExpectedTime
  ) {
    Airplane ret = new Airplane(
        callsign, coordinate, sqwk, airplaneType,
        heading, altitude, speed, false,
        entryPoint, delayInitialMinutes, delayExpectedTime
    );
    return ret;
  }

  public static Airplane createDeparture(
      Callsign callsign, Squawk sqwk, AirplaneType airplaneType,
      ActiveRunwayThreshold activeRunwayThreshold,
      Navaid exitPoint, int delayInitialMinutes, EDayTimeStamp delayExpectedTime) {
    Airplane ret = new Airplane(
        callsign, activeRunwayThreshold.getCoordinate(), sqwk, airplaneType,
        activeRunwayThreshold.getCourseInt(), activeRunwayThreshold.getParent().getParent().getAltitude(),
        0, true, exitPoint, delayInitialMinutes, delayExpectedTime
    );
    return ret;
  }



  //  private final AirplaneWriteAdvanced airplaneWriteAdvanced = new AirplaneWriteAdvanced();
//  private final Airplane4Display plane4Display = new Airplane4Display();
//  private final AirplaneType airplaneType;
  private final Squawk sqwk;
  private final AirplaneFlightModule flightModule;
  private final ShaModule sha;
  private final EmergencyModule emergencyModule;
  private final MrvaAirproxModule mrvaAirproxModule;
  private final AtcModule atcModule;
  private final PilotDataModule pilotDataModule;
  private final DivertModule divertModule;
  private final Mood mood;
  private final FlightDataRecorder fdr;
  private final CockpitVoiceRecorder cvr;
  private final AirplaneType airplaneType;
  private Coordinate coordinate;
  private State state;
  private Pilot pilot;

  private Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneType,
                   int heading, int altitude, int speed, boolean isDeparture,
                   Navaid entryExitPoint, int delayInitialMinutes, EDayTimeStamp delayExpectedTime) {


    this.sqwk = sqwk;
    this.flightModule = new AirplaneFlightModule(
        callsign, delayInitialMinutes, delayExpectedTime, isDeparture);

    this.sha = new ShaModule(imp, heading, altitude, speed, airplaneType);
    this.emergencyModule = new EmergencyModule();
    this.mrvaAirproxModule = new MrvaAirproxModule();
    this.atcModule = new AtcModule(imp);
    this.pilotDataModule = new PilotDataModule();
    if (isDeparture)
      this.divertModule = null;
    else
      this.divertModule = new DivertModule(imp);
    this.mood = new Mood();
    this.fdr = new FlightDataRecorder(this.flightModule.getCallsign());
    this.cvr = new CockpitVoiceRecorder(this.flightModule.getCallsign());
    this.state = isDeparture ? State.holdingPoint : State.arrivingHigh;
    this.coordinate = coordinate;
    this.airplaneType = airplaneType;
  }



//  public void applyShortcut(Navaid navaid) {
//    this.routingModule.applyShortcut(navaid);
//    //TODO this is not correct. Shortcut must be checked only against only not-already-flown-through points.
//    DARoute r = this.routingModule.getAssignedRoute();
//    if (r == null) return;
//    if (r.getNavaids().isEmpty()) return;
//    if (r.getNavaids().getLast().equals(navaid)) {
//      if (Airplane.this.flightModule.isArrival()) {
//        if (Airplane.this.sha.getAltitude() > 1e4)
//          mood.experience(Mood.ArrivalExperience.shortcutToIafAbove100);
//      } else {
//        if (Airplane.this.sha.getAltitude() > 1e4)
//          mood.experience(Mood.DepartureExperience.shortcutToExitPointBelow100);
//        else
//          mood.experience(Mood.DepartureExperience.shortctuToExitPointAbove100);
//      }
//    }
//  }

  public void elapseSecond() {

    processMessages();
    this.pilot.elapseSecond();
    this.atcModule.elapseSecond();
    this.divertModule.elapseSecond();

    this.sha.elapseSecond();
    updateCoordinates();

    logToFdr();

    //printAfterCommands();
    //this.recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  private void logToFdr() {
    fdr.log(
        coordinate,
        sha.getHeading(), sha.getTargetHeading(),
        sha.getAltitude(), sha.getVerticalSpeed(), sha.getTargetAltitude(),
        sha.getSpeed(), sha.getGS(), sha.getTargetSpeed(),
        state
    );
  }

  private IPilotPlane ipp;
  private IModulePlane imp;



//  @Override // IAirplaneWriteSimple
//  public IAirplaneWriteAdvanced getAdvanced() {
//    return null;
//  }
//
//  @Override // IAirplaneRO
//  public IAtcModuleRO getAtcModule() {
//    return this.atcModule;
//  }
//
//  @Override // IAirplaneRO
//  public IBehaviorModuleRO getBehaviorModule() {
//    return this.behaviorModule;
//  }
//
//  @Override // IAirplaneRO
//  public Coordinate getCoordinate() {
//    return this.coordinate;
//  }
//
//  @Override // IAirplaneRO
//  public IDivertModuleRO getDivertModule() {
//    return this.divertModule;
//  }
//
//  @Override // IAirplaneRO
//  public IEmergencyModuleRO getEmergencyModule() {
//    return this.emergencyModule;
//  }

  //
//  public AdvancedReader getAdvanced() {
//    return this.advancedReader;
//  }
//
//  public double getAltitude() {
//    return this.sha.getAltitude();
//  }
//
//  public Route getAssigneRoute() {
//    return this.pilot.getRoutingModule().getAssignedRoute();
//  }
//
//  public ActiveRunwayThreshold getAssignedRunwayThresholdForLanding() {
//    ActiveRunwayThreshold ret = tryGetAssignedRunwayThresholdForLanding();
//    if (ret == null) {
//      throw new EApplicationException(this.getFlightModule().getCallsign().toString() + " has no assigned departure/arrival threshold.");
//    }
//    return ret;
//  }
//
//  public Coordinate getCoordinate() {
//    return this.coordinate;
//  }
//
//  public Navaid getEntryExitPoint() {
//    return pilot.getRoutingModule().getEntryExitPoint();
//  }
//
//  public MoodResult getEvaluatedMood() {
//    MoodResult ret = this.mood.evaluate(this.flightModule.getCallsign(), this.flightModule.getFinalDelayMinutes());
//    return ret;
//  }
//
//  @Override // IAirplaneRO
//  public IAirplaneFlightRO getFlightModule() {
//    return this.flightModule;
//  }

  //region Inner classes


//  public class Airplane4Pilot {
//
//    public void divert() {
//      Airplane.this.flightModule.divert();
//    }
//
//    public void evaluateMoodForShortcut(Navaid navaid) {
//      Route r = getAssigneRoute();
//      if (r == null) return;
//      if (r.getNavaids().isEmpty()) return;
//      if (r.getNavaids().getLast().equals(navaid)) {
//        if (Airplane.this.flightModule.isArrival()) {
//          if (Airplane.this.sha.getAltitude() > 1e4)
//            mood.experience(Mood.ArrivalExperience.shortcutToIafAbove100);
//        } else {
//          if (Airplane.this.sha.getAltitude() > 1e4)
//            mood.experience(Mood.DepartureExperience.shortcutToExitPointBelow100);
//          else
//            mood.experience(Mood.DepartureExperience.shortctuToExitPointAbove100);
//        }
//      }
//    }
//
//    public Mood getMood() {
//      return Airplane.this.mood;
//    }
//
//    public IAirplaneRO getPlane() {
//      return Airplane.this.airplaneRO;
//    }
//
//    public ShaModule getSha() {
//      return Airplane.this.sha;
//    }
//
//    public void passMessage(Atc atc, SpeechList saidText) {
//      Message m = new Message(Airplane.this, atc, saidText);
//      Acc.messenger().send(m);
//    }
//
//    public void setNavigator(INavigator navigator) {
//      Airplane.this.sha.setNavigator(navigator);
//    }
//
//    public void setTakeOffPosition(Coordinate coordinate) {
//      assert coordinate != null;
//      Airplane.this.coordinate = coordinate;
//    }
//
//    public void setxState(State state) {
//      Airplane.this.state = state;
//      if (flightModule.getFinalDelayMinutes() == null) {
//        if ((Airplane.this.flightModule.isArrival() && state == State.landed)
//            || (Airplane.this.flightModule.isDeparture() && state == State.departingLow)) {
//          flightModule.evaluateFinalDelayMinutes();
//        }
//      }
//    }
//  }


//  public class AdvancedReader {
//
//    public Coordinate getCoordinate() {
//      return coordinate;
//    }
//
//    public Navaid getDepartureLastNavaid() {
//      if (Airplane.this.flightModule.isDeparture() == false)
//        throw new EApplicationException(sf(
//            "This method should not be called on departure aircraft %s.",
//            Airplane.this.flightModule.getCallsign().toString()));
//
//      Navaid ret = Airplane.this.pilot.getRoutingModule().getAssignedRoute().getMainNavaid();
//      return ret;
//    }
//
//    public String getHeadingS() {
//      return String.format("%1$03d", (int) Airplane.this.getHeading());
//    }
//
//    public int getTargetAltitude() {
//      return Airplane.this.sha.getTargetAltitude();
//    }
//
//    public int getTargetHeading() {
//      return Airplane.this.sha.getTargetHeading();
//    }
//
//    public String getTargetHeadingS() {
//      return String.format("%1$03d", getTargetHeading());
//    }
//
//    public double getTargetSpeed() {
//      return Airplane.this.sha.getTargetSpeed();
//    }
//
//    public boolean isOnWayToPassDeparturePoint() {
//      Navaid n = this.getDepartureLastNavaid();
//      boolean ret = Airplane.this.pilot.isOnWayToPassPoint(n);
//      return ret;
//    }
//  }


//  public class Airplane4Command {
//
//    public boolean isEmergency() {
//      return Airplane.this.isEmergency();
//    }
//
//    public State getState() {
//      return state;
//    }
//
//    public Pilot.Pilot4Command getPilot() {
//      return pilot.pilot4Command;
//    }
//
//    public Coordinate getCoordinate() {
//      return coordinate;
//    }
//
//    public AirplaneType getType() {
//      return airplaneType;
//    }
//
//    public double getAltitude() {
//      return altitude.getValue();
//    }
//
//    public int getAltitudeOrders() {
//      return targetAltitude;
//    }
//
//    public double getHeading() {
//      return heading.getValue();
//    }
//
//    public Callsign getCallsign() {
//      return callsign;
//    }
//
//    public void setTakeOffPosition(Coordinate coordinate) {
//      Airplane.this.coordinate = coordinate;
//    }
//
//    public boolean isArrival() {
//      return Airplane.this.isArrival();
//    }
//
//  }
//
//  public class Airplane4Navigator {
//    public int getTargetHeading() {
//      return Airplane.this.getTargetHeading();
//    }
//
//    public void setTargetHeading(int heading) {
//      Airplane.this.setTargetHeading(heading);
//    }
//
//    public Coordinate getCoordinates() {
//      return Airplane.this.coordinate;
//    }
//  }

  //endregion

//  @Override // IAirplaneRO
//  public IMrvaAirproxModule getMrvaAirproxModule() {
//    return this.mrvaAirproxModule;
//  }
//
//  @Override // IMessageParticipant
//  public String getName() {
//    return this.flightModule.getCallsign().toString();
//  }
//
//  @Override
//  public Airplane4Display getPlane4Display() {
//    return this.plane4Display;
//  }
//
//  @Override // IAirplaneWriteSimple
//  public FlightRecorder getRecorderModule() {
//    return this.flightRecorder;
//  }
//
//  @Override // IAirplaneRO
//  public IRoutingModuleRO getRoutingModule() {
//    return this.routingModule;
//  }
//
//  @Override // IAirplaneRO
//  public IShaRO getSha() {
//    return this.sha;
//  }
//
//  @Override // IAirplaneRO
//  public Squawk getSqwk() {
//    return this.sqwk;
//  }
//
//  @Override // IAirplaneRO
//  public State getState() {
//    return this.state;
//  }
//
//  @Override // IAirplaneRO
//  public AirplaneType getType() {
//    return this.airplaneType;
//  }
//
//  public void increaseAirprox(AirproxType airproxType) {
//    this.mrvaAirproxModule.increaseAirprox(airproxType);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void processRadarContactConfirmation() {
//    this.atcModule.setHasRadarContact();
//  }
//
//  public void resetAirprox() {
//    this.mrvaAirproxModule.resetAirprox();
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void sendMessage(Atc atc, SpeechList speechList) {
//    Message m = new Message(this, atc, speechList);
//    Acc.messenger().send(m);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setAltitudeRestriction(Restriction altitudeRestriction) {
//    this.sha.setAltitudeRestriction(altitudeRestriction);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setBehaviorAndState(Behavior behavior, State state) {
//    throw new ToDoException(); // TODO move to advanced
//  }
//
//  @Override // IAirplane4Atc
//  public void setHoldingPointState(ActiveRunwayThreshold threshold) {
//    this.airplaneWriteAdvanced.setHoldingPointState(
//        threshold.getCoordinate(),
//        (int) Math.round(threshold.getCourse()));
//  }
//
//  @Override // IAirplane4Mrva
//  public void setMrvaError(boolean value) {
//    this.mrvaAirproxModule.setMrvaError(value);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setNavigator(INavigator navigator) {
//    assert navigator != null;
//    this.sha.setNavigator(navigator);
//  }
//
//  @Override // IAirplane4Atc
//  public void setRouting(DARoute r, ActiveRunwayThreshold runwayThreshold) {
//    this.airplaneWriteAdvanced.setRouting(r, runwayThreshold);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setSpeedRestriction(Restriction speedRestriction) {
//    this.sha.setSpeedRestriction(speedRestriction);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setState(State state) {
//    this.setxState(state);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setTargetAltitude(int altitude) {
//    this.sha.setNavigator(
//        new ToCoordinateNavigator(coordinate));
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setTargetCoordinate(Coordinate coordinate) {
//
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setTargetHeading(double targetHeading) {
//    this.sha.setNavigator(
//        new HeadingNavigator(targetHeading));
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setTargetHeading(double heading, boolean isLeftTurned) {
//    this.sha.setNavigator(
//        new HeadingNavigator(heading,
//            isLeftTurned ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right));
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setTargetSpeed(int targetSpeed) {
//    this.sha.setTargetSpeed(targetSpeed);
//  }
//
//  @Override // IAirplaneWriteSimple
//  public void setxState(State state) {
//    this.state = state;
//  }
//
//  @XmlConstructor
//  private Airplane() {
//    this.sqwk = null;
//    this.airplaneType = null;
//    this.flightModule = new AirplaneFlightModule(null, 0, null, false);
//    this.pilot = new Pilot(this.new Airplane4Pilot(), null, null);
//    this.mood = null;
//  }

//  @Override // IAirplaneWriteSimple
//  public void tuneAtc(Atc atc) {
//    this.atcModule.changeAtc(atc);
//  }

  //
//  public Pilot getPilot() {
//    return this.pilot;
//  }
//
//  public Airplane4Display getPlane4Display() {
//    return this.plane4Display;
//  }
//
//  public ShaModule getSha() {
//    return this.sha;
//  }
//
//  public double getSpeed() {
//    return this.sha.getSpeed();
//  }
//
//  public Squawk getSqwk() {
//    return sqwk;
//  }
//
//  public State getState() {
//    return state;
//  }
//
//  public double getTAS() {
//    double m = 1 + this.sha.getAltitude() / 100000d;
//    double ret = this.sha.getSpeed() * m;
//    return ret;
//  }
//
//  public Atc getTunedAtc() {
//    return pilot.getAtcModule().getTunedAtc();
//  }
//
//  public AirplaneType getType() {
//    return this.airplaneType;
//  }
//
//  public double getVerticalSpeed() {
//    return this.sha.getVerticalSpeed();
//  }
//
//  public void save(XElement elm) {
//    LoadSave.saveField(elm, this, "callsign");
//    LoadSave.saveField(elm, this, "sqwk");
//    LoadSave.saveField(elm, this, "airplaneType");
//    LoadSave.saveField(elm, this, "delayInitialMinutes");
//    LoadSave.saveField(elm, this, "delayExpectedTime");
//    LoadSave.saveField(elm, this, "departure");
//    LoadSave.saveField(elm, this, "targetHeading");
//    LoadSave.saveField(elm, this, "targetHeadingLeftTurn");
//    LoadSave.saveField(elm, this, "targetAltitude");
//    LoadSave.saveField(elm, this, "targetSpeed");
//    LoadSave.saveField(elm, this, "state");
//    LoadSave.saveField(elm, this, "lastVerticalSpeed");
//    LoadSave.saveField(elm, this, "airprox");
//    LoadSave.saveField(elm, this, "mrvaError");
//    LoadSave.saveField(elm, this, "delayResult");
//    LoadSave.saveField(elm, this, "emergencyWanishTime");
//    LoadSave.saveField(elm, this, "coordinate");
//    LoadSave.saveField(elm, this, "heading");
//    LoadSave.saveField(elm, this, "speed");
//    LoadSave.saveField(elm, this, "altitude");
//    LoadSave.saveField(elm, this, "mood");
//
//    XElement tmp = new XElement("pilot");
//    this.pilot.save(tmp);
//    elm.addElement(tmp);
//
//  }
//
//  public void setHoldingPointState(Coordinate coordinate, double course) {
//    assert this.state == State.holdingPoint;
//    this.coordinate = coordinate;
//    this.sha.setNavigator(
//        new HeadingNavigator(course));
//  }
//
//  @Override
//  public String toString() {
//    return this.flightModule.getCallsign().toString();
//  }
//
//  public ActiveRunwayThreshold tryGetAssignedRunwayThresholdForLanding() {
//    ActiveRunwayThreshold ret;
//    NewApproachInfo cai = pilot.tryGetAssignedApproach();
//    if (cai == null) {
//      ret = null;
//    } else {
//      ret = cai.getThreshold();
//    }
//    return ret;
//  }
//
//  public ActiveRunwayThreshold tryGetCurrentApproachRunwayThreshold() {
//    NewApproachInfo app = this.pilot.tryGetAssignedApproach();
//    ActiveRunwayThreshold ret;
//    if (app == null)
//      ret = null;
//    else
//      ret = app.getThreshold();
//    return ret;
//  }
//
//  public void updateAssignedRouting(Route route, ActiveRunwayThreshold expectedRunwayThreshold) {
//    pilot.getRoutingModule().setRouting(route, expectedRunwayThreshold);
//  }
//
  //region Private methods

//
//  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
//    return pilot.getRoutingModule().getExpectedRunwayThreshold();
//  }
//
//  public AirplaneFlightModule getFlightModule() {
//    return this.flightModule;
//  }
//
//  public FlightRecorder getFlightRecorder() {
//    return flightRecorder;
//  }
//
//  public double getGS() {
//    return getTAS();
//  }
//
//  public double getHeading() {
//    return this.sha.getHeading();
//  }
//
//  public Mood getMood() {
//    return this.mood;
//  }
//
//  public MrvaAirproxModule getMrvaAirproxModule() {
//    return mrvaAirproxModule;
//  }
//

  private void processMessages() {
    IList<Message> msgs = LocalInstanceProvider.getMessenger().getMessagesByListener(
        Participant.createAirplane(this.flightModule.getCallsign()), true);

    // only responds to messages from tuned atc
    msgs = msgs.where(q -> q.getSource().equals(Participant.createAtc(this.atcModule.getTunedAtc())));

    // extract contents
    IList<IMessageContent> contents = msgs.select(q -> q.getContent());
    for (IMessageContent c : contents) {
      SpeechList cmds;
      if (c instanceof SpeechList)
        cmds = (SpeechList) c;
      else {
        cmds = new SpeechList((ISpeech) c);
      }
      this.pilotDataModule.addNewSpeeches(cmds);
    }
  }

  private void updateCoordinates() {
    double dist = this.sha.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, this.sha.getHeading(), dist);

    // add wind if flying
    if (this.state.is(
        State.holdingPoint,
        State.takeOffRoll,
        State.landed
    ) == false) {
      Weather weather = Acc.weather();
      newC = Coordinates.getCoordinate(
          newC,
          weather.getWindHeading(),
          UnitProvider.ftToNm(weather.getWindSpeedOrWindGustSpeed()));
    }

    this.coordinate = newC;
  }
//  //endregion
}
