package eng.jAtcSim.lib.airplanes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.behaviors.*;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteAdvanced;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.interfaces.modules.*;
import eng.jAtcSim.lib.airplanes.modules.*;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.navigators.INavigator;
import eng.jAtcSim.lib.airplanes.navigators.ToCoordinateNavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertingNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public class Airplane implements IAirplaneWriteSimple {

    public class Airplane4Display {

    public int altitude() {
      return Airplane.this.sha.getAltitude();
    }

    public Callsign callsign() {
      return Airplane.this.flightModule.getCallsign();
    }

    public Coordinate coordinate() {
      return Airplane.this.coordinate;
    }

    public Navaid entryExitPoint() {
      return Airplane.this.routingModule.getEntryExitFix();
    }

    public AirproxType getAirprox() {
      return Airplane.this.mrvaAirproxModule.getAirprox();
    }

    public Route getAssignedRoute() {
      return Airplane.this.routingModule.getAssignedRoute();
    }

    public ActiveRunwayThreshold getExpectedRunwayThreshold() {
      return Airplane.this.routingModule.getAssignedRunwayThreshold();
    }

    public boolean hasRadarContact() {
      return Airplane.this.atcModule.hasRadarContact();
    }

    public int heading() {
      return Airplane.this.sha.getHeading();
    }

    public int ias() {
      return Airplane.this.sha.getSpeed();
    }

    public boolean isDeparture() {
      return Airplane.this.flightModule.isDeparture();
    }

    public boolean isEmergency() {
      return Airplane.this.emergencyModule.isEmergency();
    }

    public boolean isMrvaError() {
      return Airplane.this.mrvaAirproxModule.isMrvaError();
    }

    public AirplaneType planeType() {
      return Airplane.this.airplaneType;
    }

    public Atc responsibleAtc() {
      return Acc.prm().getResponsibleAtc(Airplane.this);
    }

    public Squawk squawk() {
      return Airplane.this.sqwk;
    }

    public String status() {
      Behavior behavior = Airplane.this.behaviorModule.get();
    if (behavior instanceof BasicBehavior)
      return behavior instanceof ArrivalBehavior ? "Arriving" : "Departing";
    else if (behavior instanceof HoldBehavior)
      return "Holding";
    else if (behavior instanceof NewApproachBehavior)
      return "In approach " + Airplane.this.routingModule.getAssignedRunwayThreshold().getName();
    else if (behavior instanceof HoldingPointBehavior)
      return "Holding point";
    else if (behavior instanceof TakeOffBehavior)
      return "Take-off";
    else
      return "???";
    }

    public int targetAltitude() {
      return Airplane.this.sha.getTargetAltitude();
    }

    public int targetHeading() {
      return Airplane.this.sha.getTargetHeading();
    }

    public int targetSpeed() {
      return Airplane.this.sha.getTargetSpeed();
    }

    public int tas() {
      return Airplane.this.sha.getTAS();
    }

    public Atc tunedAtc() {
      return Airplane.this.atcModule.getTunedAtc();
    }

    public int verticalSpeed() {
      return (int) Airplane.this.sha.getVerticalSpeed();
    }
  }

  public class AirplaneWriteAdvanced implements IAirplaneWriteAdvanced{

    @Override
    public void abortHolding() {
      if (Airplane.this.flightModule.isArrival())
        Airplane.this.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
      else
        Airplane.this.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);
      Airplane.this.adjustTargetSpeed();
    }

    @Override
    public void addExperience(Mood.ArrivalExperience experience) {
      Airplane.this.mood.experience(experience);
    }

    @Override
    public void addExperience(Mood.DepartureExperience experience) {
      Airplane.this.mood.experience(experience);
    }

    @Override
    public void clearedToApproach(NewApproachInfo newApproachInfo) {
// abort holding, only if fix was found
      if (Airplane.this.state == Airplane.State.holding) {
        this.abortHolding();
      }

      NewApproachBehavior behavior = new NewApproachBehavior(newApproachInfo);
      Airplane.this.setBehaviorAndState(behavior, Airplane.State.flyingIaf2Faf);
    }

    @Override
    public void divert(boolean isInvokedByAtc) {
      if (isInvokedByAtc) {
        if (Airplane.this.emergencyModule.isEmergency())
          this.addExperience(Mood.DepartureExperience.divertedAsEmergency);
        else if (!Acc.isSomeActiveEmergency() == false)
          this.addExperience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
        Airplane.this.divertModule.disable();
      } else {
        this.addExperience(Mood.ArrivalExperience.divertOrderedByCaptain);
      }

      Navaid divertNavaid = getDivertNavaid();
      Route route = Route.createNewVectoringByFix(divertNavaid);

      Airplane.this.flightModule.divert();
      Airplane.this.routingModule.setRoute(route);
      Airplane.this.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);

      if (!isInvokedByAtc)
        Airplane.this.sendMessage(
            new DivertingNotification(divertNavaid));
    }

    private Navaid getDivertNavaid() {
      IList<Route> rts = Acc
          .atcTwr().getRunwayConfigurationInUse()
          .getDepartures()
          .where(q -> q.isForCategory(Airplane.this.airplaneType.category))
          .getRandom()
          .getThreshold()
          .getRoutes()
          .where(q -> q.getType() == Route.eType.sid);
      Route r = rts.getRandom();
      //TODO here can null-pointer-exception occur when no route is found for threshold and category
      Navaid ret = r.getMainNavaid();
      return ret;
    }

    @Override
    public void goAround(GoingAroundNotification.GoAroundReason gaReason) {
      assert gaReason != null;

      boolean isAtcFail = EnumUtils.is(gaReason,
          new GoingAroundNotification.GoAroundReason[]{
              GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
              GoingAroundNotification.GoAroundReason.noLandingClearance,
              GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
              GoingAroundNotification.GoAroundReason.notStabilizedAirplane
          });
      if (isAtcFail)
        this.addExperience(
            Mood.ArrivalExperience.goAroundNotCausedByPilot);

      GoingAroundNotification gan = new GoingAroundNotification(gaReason);
      Airplane.this.sendMessage(gan);

      NewApproachBehavior nab = Airplane.this.behaviorModule.getAs(NewApproachBehavior.class);
      NewApproachInfo nai = nab.getApproachInfo();

      Airplane.this.sha.setTargetSpeed(Airplane.this.airplaneType.vDep);
      Airplane.this.sha.setTargetAltitude(Airplane.this.sha.getAltitude());
      Airplane.this.sha.setNavigator(
          new HeadingNavigator(nai.getRunwayThreshold().getCourse()));

      SpeechList<IFromAtc> gas = new SpeechList<>(nai.getGaCommands());
      this.prepareGoAroundRouting(gas, nai);
      Airplane.this.routingModule.setRoute(gas);

      Airplane.this.setBehaviorAndState(
          new TakeOffBehavior(
              Airplane.this.airplaneType.category,
              Airplane.this.getRoutingModule().getAssignedRunwayThreshold()),
          Airplane.State.takeOffGoAround);
    }

    private boolean isBeforeRunwayThreshold(NewApproachInfo nai) {
      double dist = Coordinates.getDistanceInNM(Airplane.this.coordinate, nai.getRunwayThreshold().getCoordinate());
      double hdg = Coordinates.getBearing(Airplane.this.coordinate, nai.getRunwayThreshold().getCoordinate());
      boolean ret;
      if (dist < 3)
        ret = false;
      else {
        ret = Headings.isBetween(nai.getRunwayThreshold().getCourse() - 70, hdg, nai.getRunwayThreshold().getCourse() + 70);
      }
      return ret;
    }

    private void prepareGoAroundRouting(SpeechList<IFromAtc> gaRoute, NewApproachInfo nai) {
      ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
      if (gaRoute.get(0) instanceof ChangeAltitudeCommand) {
        cac = (ChangeAltitudeCommand) gaRoute.get(0);
        gaRoute.removeAt(0);
      }
      gaRoute.insert(0, new ChangeHeadingCommand((int) nai.getRunwayThreshold().getCourse(), ChangeHeadingCommand.eDirection.any));

      // check if is before runway threshold.
      // if is far before, then first point will still be runway threshold
      if (isBeforeRunwayThreshold(nai)) {
        String runwayThresholdNavaidName =
            nai.getRunwayThreshold().getParent().getParent().getIcao() + ":" + nai.getRunwayThreshold().getName();
        Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
        gaRoute.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
        gaRoute.insert(1, new ThenCommand());
      }

      if (cac != null)
        gaRoute.insert(0, cac);
    }

    @Override
    public void hold(Navaid navaid, int inboundRadial, boolean leftTurn) {
      HoldBehavior hold = new HoldBehavior(Airplane.this,
          navaid,
          inboundRadial,
          leftTurn);
      Airplane.this.setBehaviorAndState(hold, Airplane.State.holding);
    }

    @Override
    public void setRoute(SpeechList route) {
      Airplane.this.routingModule.setRoute(route);
    }

    @Override
    public void setRouting(Route route, ActiveRunwayThreshold activeRunwayThreshold) {
      Airplane.this.routingModule.setRouting(route, activeRunwayThreshold);
    }

    @Override
    public void takeOff(ActiveRunwayThreshold runwayThreshold) {
      Airplane.this.coordinate = runwayThreshold.getCoordinate();
      Airplane.this.setBehaviorAndState(
          new TakeOffBehavior(Airplane.this.airplaneType.category, runwayThreshold),
          Airplane.State.takeOffRoll);
      Airplane.this.sha.setTargetSpeed(
          Airplane.this.airplaneType.getV2());
      Airplane.this.sha.setNavigator(
          new HeadingNavigator(runwayThreshold.getCourse()));
    }
  }

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
  private static final int MINIMAL_DIVERT_TIME_MINUTES = 45;
  private static final int MAXIMAL_DIVERT_TIME_MINUTES = 120;
  private static final double secondFraction = 1 / 60d / 60d;

  public static Airplane load(XElement elm) {

    throw new ToDoException();

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
  }

  private static ETime generateDivertTime(boolean isDeparture) {
    ETime divertTime = null;
    if (!isDeparture) {
      int divertTimeMinutes = Acc.rnd().nextInt(MINIMAL_DIVERT_TIME_MINUTES, MAXIMAL_DIVERT_TIME_MINUTES);
      divertTime = Acc.now().addMinutes(divertTimeMinutes);
    }
    return divertTime;
  }
  private final AirplaneWriteAdvanced airplaneWriteAdvanced = new AirplaneWriteAdvanced();
  private final AirplaneType airplaneType;
  private final Squawk sqwk;
  private final AirplaneFlightModule flightModule;
  private final ShaModule sha = new ShaModule(this);
  private final EmergencyModule emergencyModule = new EmergencyModule(this);
  private final MrvaAirproxModule mrvaAirproxModule = new MrvaAirproxModule(this);
  private final BehaviorModule behaviorModule = new BehaviorModule(this);
  private final AtcModule atcModule = new AtcModule(this);
  private final RoutingModule routingModule = new RoutingModule(this);
  private final DivertModule divertModule = new DivertModule(this);
  private final Mood mood;
  //  private final AdvancedReader advancedReader = new AdvancedReader();
//  @XmlIgnore
//  private final Airplane4Display plane4Display = new Airplane4Display();
  private Coordinate coordinate;
  private State state;
  @XmlIgnore
  private FlightRecorder flightRecorder = null;

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
                  int heading, int altitude, int speed, boolean isDeparture,
                  Navaid entryExitPoint, int delayInitialMinutes, ETime delayExpectedTime) {

    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneType = airplaneSpecification;
    this.state = isDeparture ? State.holdingPoint : State.arrivingHigh;

    this.flightModule = new AirplaneFlightModule(callsign, delayInitialMinutes, delayExpectedTime, isDeparture);
    this.sha.init(heading, altitude, speed, airplaneSpecification, Acc.airport().getAltitude());
    this.flightRecorder = FlightRecorder.create(callsign);
    this.mood = new Mood();
  }

  @Override // IAirplaneWriteSimple
  public void adjustTargetSpeed() {
    int minOrdered;
    int maxOrdered;
    Restriction speedRestriction = this.sha.getSpeedRestriction();
    if (speedRestriction != null) {
      switch (speedRestriction.direction) {
        case exactly:
          minOrdered = speedRestriction.value;
          maxOrdered = speedRestriction.value;
          break;
        case atLeast:
          minOrdered = speedRestriction.value;
          maxOrdered = Integer.MAX_VALUE;
          break;
        case atMost:
          minOrdered = Integer.MIN_VALUE;
          maxOrdered = speedRestriction.value;
          break;
        default:
          throw new EEnumValueUnsupportedException(speedRestriction.direction);
      }
    } else {
      minOrdered = Integer.MIN_VALUE;
      maxOrdered = Integer.MAX_VALUE;
    }
    int ts;
    switch (this.state) {
      case holdingPoint:
      case landed:
        ts = 0;
        break;
      case takeOffRoll:
      case takeOffGoAround:
        ts = this.getType().vR + 10;
        break;
      case departingLow:
      case arrivingLow:
        ts = getBoundedValueIn(minOrdered, Math.min(250, this.airplaneType.vCruise), maxOrdered);
        break;
      case departingHigh:
      case arrivingHigh:
        ts = getBoundedValueIn(minOrdered, Math.min(287, this.airplaneType.vCruise), maxOrdered);
        break;
      case arrivingCloseFaf:
      case flyingIaf2Faf:
        ts = getBoundedValueIn(minOrdered, Math.min(287, this.airplaneType.vMinClean + 15), maxOrdered);
        break;
      case approachEnter:
        ts = getBoundedValueIn(minOrdered, Math.min(this.airplaneType.vMaxApp, this.airplaneType.vMinClean), maxOrdered);
        break;
      case approachDescend:
        ts = getBoundedValueIn(minOrdered, this.airplaneType.vApp, maxOrdered);
        break;
      case longFinal:
      case shortFinal:
        minOrdered = Math.max(minOrdered, this.airplaneType.vMinApp);
        maxOrdered = Math.min(maxOrdered, this.airplaneType.vMaxApp);
        ts = getBoundedValueIn(minOrdered, this.airplaneType.vApp, maxOrdered);
        break;
      case holding:
        if (this.sha.getTargetAltitude() > 10000)
          ts = getBoundedValueIn(minOrdered, Math.min(250, this.airplaneType.vCruise), maxOrdered);
        else
          ts = getBoundedValueIn(minOrdered, Math.min(220, this.airplaneType.vCruise), maxOrdered);
        break;
      default:
        throw new EEnumValueUnsupportedException(this.state);
    }
    this.sha.setTargetSpeed(ts);
  }

  @Override // IAirplaneWriteSimple
  public void applyShortcut(Navaid navaid) {
    this.routingModule.applyShortcut(navaid);
    Route r = this.routingModule.getAssignedRoute();
    if (r == null) return;
    if (r.getNavaids().isEmpty()) return;
    if (r.getNavaids().getLast().equals(navaid)) {
      if (Airplane.this.flightModule.isArrival()) {
        if (Airplane.this.sha.getAltitude() > 1e4)
          mood.experience(Mood.ArrivalExperience.shortcutToIafAbove100);
      } else {
        if (Airplane.this.sha.getAltitude() > 1e4)
          mood.experience(Mood.DepartureExperience.shortcutToExitPointBelow100);
        else
          mood.experience(Mood.DepartureExperience.shortctuToExitPointAbove100);
      }
    }
  }

  @Override // IAirplaneWriteSimple
  public IAirplaneWriteAdvanced getAdvanced() {
    return null;
  }

  @Override // IAirplaneRO
  public IAtcModuleRO getAtcModule() {
    return this.atcModule;
  }

  @Override // IAirplaneRO
  public IBehaviorModuleRO getBehaviorModule() {
    return this.behaviorModule;
  }

  @Override // IAirplaneRO
  public Coordinate getCoordinate() {
    return this.coordinate;
  }

  @Override // IAirplaneRO
  public IDivertModuleRO getDivertModule() {
    return this.divertModule;
  }

  @Override // IAirplaneRO
  public IEmergencyModuleRO getEmergencyModule() {
    return this.emergencyModule;
  }

  @Override // IAirplaneRO
  public IAirplaneFlightRO getFlightModule() {
    return this.flightModule;
  }

  @Override // IMessageParticipant
  public String getName() {
    return this.flightModule.getCallsign().toString();
  }

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

  @Override // IAirplaneWriteSimple
  public FlightRecorder getRecorderModule() {
    return this.flightRecorder;
  }

  @Override // IAirplaneRO
  public IRoutingModuleRO getRoutingModule() {
    return this.routingModule;
  }

  @Override // IAirplaneRO
  public IShaRO getSha() {
    return this.sha;
  }

  @Override // IAirplaneRO
  public Squawk getSqwk() {
    return this.sqwk;
  }

  @Override // IAirplaneRO
  public State getState() {
    return this.state;
  }

  @Override // IAirplaneWriteSimple
  public void setState(State state) {
    this.setxState(state);
  }

  @Override // IAirplaneRO
  public AirplaneType getType() {
    return this.airplaneType;
  }

  @Override // IAirplaneWriteSimple
  public void processRadarContactConfirmation() {
    this.atcModule.setHasRadarContact();
  }

  @Override // IAirplaneWriteSimple
  public void sendMessage(Atc atc, SpeechList speechList) {
    Message m = new Message(this, atc, speechList);
    Acc.messenger().send(m);
  }

  @Override // IAirplaneWriteSimple
  public void setAltitudeRestriction(Restriction altitudeRestriction) {
    this.sha.setAltitudeRestriction(altitudeRestriction);
  }

  @Override // IAirplaneWriteSimple
  public void setBehaviorAndState(Behavior behavior, State state) {
    throw new ToDoException(); // TODO move to advanced
  }

  @Override // IAirplaneWriteSimple
  public void setNavigator(INavigator navigator) {
    assert navigator != null;
    this.sha.setNavigator(navigator);
  }

  @Override // IAirplaneWriteSimple
  public void setSpeedRestriction(Restriction speedRestriction) {
    this.sha.setSpeedRestriction(speedRestriction);
  }

  @Override // IAirplaneWriteSimple
  public void setTargetAltitude(int altitude) {
    this.sha.setNavigator(
        new ToCoordinateNavigator(coordinate));
  }

  @Override // IAirplaneWriteSimple
  public void setTargetCoordinate(Coordinate coordinate) {

  }

  @Override // IAirplaneWriteSimple
  public void setTargetHeading(double targetHeading) {
    this.sha.setNavigator(
        new HeadingNavigator(targetHeading));
  }

  @Override // IAirplaneWriteSimple
  public void setTargetHeading(double heading, boolean isLeftTurned) {
    this.sha.setNavigator(
        new HeadingNavigator(heading,
            isLeftTurned ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right));
  }

  @Override // IAirplaneWriteSimple
  public void setTargetSpeed(int targetSpeed) {
    this.sha.setTargetSpeed(targetSpeed);
  }

  @Override // IAirplaneWriteSimple
  public void setxState(State state) {
    this.state = state;
  }

  @Override // IAirplaneWriteSimple
  public void tuneAtc(Atc atc) {
    this.atcModule.changeAtc(atc);
  }

  private int getBoundedValueIn(int min, int value, int max) {
    if (value < min)
      value = min;
    if (value > max)
      value = max;
    return value;
  }
//
//  @XmlConstructor
//  private Airplane() {
//    this.sqwk = null;
//    this.airplaneType = null;
//    this.flightModule = new AirplaneFlightModule(null, 0, null, false);
//    this.pilot = new Pilot(this.new Airplane4Pilot(), null, null);
//    this.mood = null;
//  }
//
//  public void elapseSecond() {
//
//    processMessages();
//    drivePlane();
//    this.sha.elapseSecond();
//    updateCoordinates();
//
//    flightRecorder.logFDR(this, this.pilot);
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
//  public Navaid getEntryExitFix() {
//    return pilot.getRoutingModule().getEntryExitPoint();
//  }
//
//  public MoodResult getEvaluatedMood() {
//    MoodResult ret = this.mood.evaluate(this.flightModule.getCallsign(), this.flightModule.getFinalDelayMinutes());
//    return ret;
//  }
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
//  //region Private methods
//  private void drivePlane() {
//    pilot.elapseSecond();
//  }
//
//  private boolean isValidMessageForAirplane(IMessageContent msg) {
//    if (msg instanceof IFromAtc)
//      return true;
//    else if (msg instanceof SpeechList) {
//      for (ISpeech o : (SpeechList<ISpeech>) msg) {
//        if (!(o instanceof IFromAtc)) {
//          return false;
//        }
//      }
//      return true;
//    }
//    return false;
//  }
//
//  private void processCommands(SpeechList speeches) {
//    this.pilot.getRoutingModule().addNewSpeeches(speeches);
//  }
//
//  private void processMessage(Message msg) {
//    // if item from non-tuned ATC, then is ignored
//    if (msg.getSource() != this.pilot.getAtcModule().getTunedAtc()) {
//      return;
//    }
//
//    SpeechList cmds;
//    IMessageContent s = msg.getContent();
//    if (isValidMessageForAirplane(s)) {
//      if (s instanceof SpeechList)
//        cmds = (SpeechList) s;
//      else {
//        cmds = new SpeechList();
//        cmds.add((ISpeech) s);
//      }
//    } else {
//      throw new EApplicationException("Airplane can only deal with messages containing \"IFromAtc\" or \"List<IFromAtc>\".");
//    }
//
//    processCommands(cmds);
//  }
//
//  private void processMessages() {
//    IList<Message> msgs = Acc.messenger().getMessagesByListener(this, true);
//    for (Message m : msgs) {
//      processMessage(m);
//    }
//  }
//
//  private void updateCoordinates() {
//    double dist = this.getGS() * secondFraction;
//    Coordinate newC
//        = Coordinates.getCoordinate(coordinate, this.sha.getHeading(), dist);
//
//    // add wind if flying
//    if (this.getState().is(
//        State.holdingPoint,
//        State.takeOffRoll,
//        State.landed
//    ) == false)
//      newC = Coordinates.getCoordinate(
//          newC,
//          Acc.weather().getWindHeading(),
//          UnitProvider.ftToNm(Acc.weather().getWindSpeedOrWindGustSpeed()));
//
//    this.coordinate = newC;
//  }
//
//  //endregion
}
