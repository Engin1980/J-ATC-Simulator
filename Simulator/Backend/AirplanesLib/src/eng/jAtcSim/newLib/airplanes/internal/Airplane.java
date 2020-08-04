package eng.jAtcSim.newLib.airplanes.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.*;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.modules.AirplaneFlightModule;
import eng.jAtcSim.newLib.airplanes.modules.AtcModule;
import eng.jAtcSim.newLib.airplanes.modules.DivertModule;
import eng.jAtcSim.newLib.airplanes.modules.EmergencyModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.ShaModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.ToCoordinateNavigator;
import eng.jAtcSim.newLib.airplanes.modules.speeches.RoutingModule;
import eng.jAtcSim.newLib.airplanes.other.CockpitVoiceRecorder;
import eng.jAtcSim.newLib.airplanes.other.FlightDataRecorder;
import eng.jAtcSim.newLib.airplanes.pilots.*;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.DivertTimeNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.DivertingNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.weather.Weather;

public class Airplane {

  public class AirplaneShaImpl implements IAirplaneSHA {
    @Override
    public int getAltitude() {
      return Airplane.this.sha.getAltitude();
    }

    @Override
    public int getHeading() {
      return Airplane.this.sha.getHeading();
    }

    @Override
    public int getSpeed() {
      return Airplane.this.sha.getSpeed();
    }

    @Override
    public Restriction getSpeedRestriction() {
      return Airplane.this.sha.getSpeedRestriction();
    }

    @Override
    public int getTargetAltitude() {
      return Airplane.this.sha.getTargetAltitude();
    }

    @Override
    public int getTargetHeading() {
      return Airplane.this.sha.getTargetHeading();
    }

    @Override
    public int getVerticalSpeed() {
      return Airplane.this.sha.getVerticalSpeed();
    }
  }

  public class AirplaneAtcImpl implements IAirplaneAtc {
    @Override
    public AtcId getTunedAtc() {
      return Airplane.this.atcModule.getTunedAtc();
    }

    @Override
    public boolean hasRadarContact() {
      return Airplane.this.atcModule.hasRadarContact();
    }
  }

  public class AirplaneFlightImpl implements IAirplaneFlight {

    @Override
    public int getEntryDelay() {
      return Airplane.this.flightModule.getEntryDelay();
    }

    @Override
    public int getExitDelay() {
      return Airplane.this.flightModule.getExitDelay();
    }
  }

  public class AirplaneRoutingImpl implements IAirplaneRouting {
    @Override
    public DARoute getAssignedRoute() {
      throw new ToDoException();
    }

    @Override
    public ActiveRunwayThreshold getAssignedRunwayThreshold() {
      throw new ToDoException();
    }

    @Override
    public Navaid getDepartureLastNavaid() {
      throw new ToDoException();
    }

    @Override
    public Navaid getEntryExitPoint() {
      return Airplane.this.routingModule.getEntryExitPoint();
    }

    @Override
    public boolean hasLateralDirectionAfterCoordinate() {
      Coordinate coordinate = tryGetTargetCoordinate();
      assert coordinate != null;
      return Airplane.this.routingModule.hasLateralDirectionAfterCoordinate(coordinate);
    }

    @Override
    public boolean isDivertable() {
      return Airplane.this.pilot.isDivertable();
    }

    @Override
    public boolean isGoingToFlightOverNavaid(Navaid n) {
      return Airplane.this.routingModule.isGoingToFlightOverNavaid(n);
    }

    @Override
    public boolean isRoutingEmpty() {
      throw new ToDoException();
    }

    @Override
    public Coordinate tryGetTargetCoordinate() {
      return Airplane.this.sha.tryGetTargetCoordinate();
    }

    @Override
    public Coordinate tryGetTargetOrHoldCoordinate() {
      Coordinate ret = tryGetTargetCoordinate();
      if (ret == null) {
        if (Airplane.this.pilot instanceof HoldPilot) {
          HoldPilot holdPilot = (HoldPilot) Airplane.this.pilot;
          ret = holdPilot.navaid.getCoordinate();
        }
      }

      return ret;
    }
  }

  public class AirplaneImpl implements IAirplane {
    private final IAirplaneAtc atc = Airplane.this.new AirplaneAtcImpl();
    private final IAirplaneFlight flight = Airplane.this.new AirplaneFlightImpl();
    private final IAirplaneRouting routing = Airplane.this.new AirplaneRoutingImpl();
    private final IAirplaneSHA sha = Airplane.this.new AirplaneShaImpl();

    @Override
    public IAirplaneAtc getAtc() {
      return atc;
    }

    @Override
    public Callsign getCallsign() {
      return Airplane.this.flightModule.getCallsign();
    }

    @Override
    public Coordinate getCoordinate() {
      return Airplane.this.coordinate;
    }

    @Override
    public IAirplaneFlight getFlight() {
      return flight;
    }

    @Override
    public IAirplaneRouting getRouting() {
      return routing;
    }

    @Override
    public IAirplaneSHA getSha() {
      return sha;
    }

    @Override
    public Squawk getSqwk() {
      return Airplane.this.sqwk;
    }

    @Override
    public AirplaneState getState() {
      return Airplane.this.state;
    }

    @Override
    public AirplaneType getType() {
      return Airplane.this.airplaneType;
    }

    @Override
    public boolean hasElapsedEmergencyTime() {
      return false;
    }

    @Override
    public boolean isArrival() {
      return Airplane.this.flightModule.isArrival();
    }

    @Override
    public boolean isEmergency() {
      return Airplane.this.emergencyModule.isEmergency();
    }
  }

  public class AirplaneWriterImpl implements IAirplaneWriter {
    @Override
    public void abortHolding() {
      if (Airplane.this.flightModule.isArrival())
        setPilotAndState(new ArrivalPilot(Airplane.this), AirplaneState.arrivingHigh);
      else
        setPilotAndState(new DeparturePilot(Airplane.this), AirplaneState.departingLow);
      throw new ToDoException("next line?");
      //Pilot.this.pilotWriteSimple.adjustTargetSpeed();
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
    public void applyShortcut(Navaid navaid) {
      Airplane.this.routingModule.applyShortcut(navaid);
    }

    @Override
    public void clearedToApproach(Approach approach, ApproachEntry entry) {
      ApproachPilot approachPilot = new ApproachPilot(Airplane.this, approach, entry);
      setPilotAndState(approachPilot, AirplaneState.flyingIaf2Faf);
    }

    @Override
    public void divert(boolean isInvokedByAtc) {
      if (isInvokedByAtc) {
        if (Airplane.this.emergencyModule.isEmergency())
          this.addExperience(Mood.DepartureExperience.divertedAsEmergency);
        else if (!Context.getAirplane().isSomeActiveEmergency() == false)
          this.addExperience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
        Airplane.this.divertModule.disable();
      } else {
        this.addExperience(Mood.ArrivalExperience.divertOrderedByCaptain);
      }

      Navaid divertNavaid = getDivertNavaid();
      DARoute route = DARoute.createNewVectoringByFix(divertNavaid);

      Airplane.this.flightModule.divert();
      setRouting(route, Airplane.this.routingModule.getRunwayThreshold());
      setPilotAndState(new DeparturePilot(Airplane.this), AirplaneState.departingLow);

      if (!isInvokedByAtc)
        this.sendMessage(
            Airplane.this.atcModule.getTunedAtc(),
            new DivertingNotification(divertNavaid.getName()));
    }

    @Override
    public CockpitVoiceRecorder getCVR() {
      return Airplane.this.cvr;
    }

    @Override
    public void goAround(GoingAroundNotification.GoAroundReason reason) {
      assert reason != null;

      boolean isAtcFail = EnumUtils.is(reason,
          new GoingAroundNotification.GoAroundReason[]{
              GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
              GoingAroundNotification.GoAroundReason.noLandingClearance,
              GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
              GoingAroundNotification.GoAroundReason.notStabilizedAirplane
          });
      if (isAtcFail)
        this.addExperience(
            Mood.ArrivalExperience.goAroundNotCausedByPilot);

      GoingAroundNotification gan = new GoingAroundNotification(reason);
      this.sendMessage(
          Airplane.this.atcModule.getTunedAtc(),
          gan);

      EAssert.isTrue(Airplane.this.pilot instanceof ApproachPilot);
      ApproachPilot prevPilot = (ApproachPilot) Airplane.this.pilot;
      Airplane.this.sha.setTargetSpeed(Airplane.this.airplaneType.vDep);
      Airplane.this.sha.setTargetAltitude(Airplane.this.sha.getAltitude());
      Airplane.this.sha.setNavigator(new HeadingNavigator(prevPilot.getRunwayThreshold().getCourse()));

      SpeechList<ICommand> gas = prevPilot.getGoAroundRouting();
      setRouting(gas);

      setPilotAndState(
          new TakeOffPilot(
              Airplane.this,
              prevPilot.getRunwayThreshold()),
          AirplaneState.takeOffGoAround);
    }

    @Override
    public void hold(Navaid navaid, int inboundRadial, LeftRight turn) {
      setPilotAndState(
          new HoldPilot(Airplane.this, navaid, inboundRadial, turn),
          AirplaneState.holding
      );
    }

    @Override
    public void processRadarContactConfirmation() {
      Airplane.this.atcModule.setHasRadarContact();
    }

    @Override
    public void raiseEmergency() {
      //TODO Implement this: how to raise an emergency
      throw new ToDoException("how to raise an emergency");
    }

    @Override
    public void reportDivertTimeLeft() {
      EAssert.isTrue(Airplane.this.flightModule.isArrival());
      EDayTimeStamp divertTime = Airplane.this.divertModule.getDivertTime();
      EDayTimeStamp now = Context.getShared().getNow().toStamp();
      int minutesLeft = (int) Math.ceil((divertTime.getValue() - now.getValue()) / 60d);
      EAssert.isTrue(minutesLeft >= 0);
      sendMessage(
          Airplane.this.atcModule.getTunedAtc(),
          new DivertTimeNotification(minutesLeft));
    }

    @Override
    public void sendMessage(AtcId atcId, SpeechList<IFromPlaneSpeech> speechList) {
      Message m = new Message(
          Participant.createAirplane(Airplane.this.getReader().getCallsign()),
          Participant.createAtc(Airplane.this.getReader().getAtc().getTunedAtc()),
          speechList);
      Context.getMessaging().getMessenger().send(m);
    }

    @Override
    public void setAltitudeRestriction(Restriction restriction) {
      EAssert.Argument.isNotNull(restriction, "restriction");
      Airplane.this.sha.setAltitudeRestriction(restriction);
    }

    @Override
    public void setRouting(IafRoute iafRoute, ActiveRunwayThreshold activeRunwayThreshold) {
      Airplane.this.routingModule.setRunwayThreshold(activeRunwayThreshold);
      Airplane.this.routingModule.setRouting(iafRoute.getRouteCommands());
    }

    @Override
    public void setRouting(IReadOnlyList<ICommand> routeCommands) {
      Airplane.this.routingModule.setRouting(routeCommands);
    }

    @Override
    public void setRouting(DARoute daRoute, ActiveRunwayThreshold activeRunwayThreshold) {
      Airplane.this.routingModule.setRunwayThreshold(activeRunwayThreshold);
      Airplane.this.routingModule.setEntryExitPoint(daRoute.getMainNavaid());
      Airplane.this.routingModule.setRouting(daRoute.getRouteCommands());
    }

    @Override
    public void setSpeedRestriction(Restriction restriction) {
      EAssert.Argument.isNotNull(restriction, "restriction");
      Airplane.this.sha.setSpeedRestriction(restriction);
    }

    @Override
    public void setState(AirplaneState state) {
      Airplane.this.state = state;
    }

    @Override
    public void setTargetAltitude(int altitudeInFt) {
      Airplane.this.sha.setTargetAltitude(altitudeInFt);
    }

    @Override
    public void setTargetCoordinate(Coordinate coordinate) {
      EAssert.Argument.isNotNull(coordinate, "coordinate");
      Airplane.this.sha.setNavigator(new ToCoordinateNavigator(coordinate));
    }

    @Override
    public void setTargetHeading(Navigator navigator) {
      EAssert.Argument.isNotNull(navigator, "navigator");
      Airplane.this.sha.setNavigator(navigator);
    }

    @Override
    public void setTargetSpeed(int speed) {
      Airplane.this.sha.setTargetSpeed(speed);
    }

    @Override
    public void startArriving() {
      setPilotAndState(
          new ArrivalPilot(Airplane.this),
          AirplaneState.arrivingHigh
      );
    }

    @Override
    public void startDeparting() {
      setPilotAndState(
          new DeparturePilot(Airplane.this),
          AirplaneState.departingLow);
    }

    @Override
    public void startHolding(Navaid navaid, int inboundRadial, LeftRight turn) {
      HoldPilot pilot = new HoldPilot(
          Airplane.this,
          navaid,
          inboundRadial,
          turn
      );
      setPilotAndState(pilot, AirplaneState.holding);
    }

    @Override
    public void startTakeOff(ActiveRunwayThreshold threshold) {
      EAssert.Argument.isNotNull(threshold, "threshold");
      TakeOffPilot pilot = new TakeOffPilot(Airplane.this, threshold);
      setPilotAndState(pilot, AirplaneState.takeOffRoll);
    }

    @Override
    public void tuneAtc(AtcId atcId) {
      EAssert.Argument.isNotNull(atcId, "atcId");
      Airplane.this.atcModule.changeAtc(atcId);
    }

    private void setPilotAndState(Pilot pilot, AirplaneState state) {
      Airplane.this.pilot = pilot;
      Airplane.this.state = state;
    }
  }

  private static final double secondFraction = 1 / 60d / 60d;

  public static Airplane createArrival(ArrivalAirplaneTemplate template, Squawk sqwk, AtcId initialAtcId) {
    Airplane ret = new Airplane(
        template.getCallsign(), template.getCoordinate(), sqwk, template.getAirplaneType(),
        template.getHeading(), template.getAltitude(), template.getSpeed(), false,
        template.getEntryPoint().getNavaid(), template.getExpectedExitTime(), template.getEntryDelay(),
        initialAtcId
    );
    return ret;
  }

  public static Airplane createDeparture(DepartureAirplaneTemplate template, Squawk sqwk, AtcId initialAtcId) {
    Airplane ret = new Airplane(
        template.getCallsign(), Context.getArea().getAirport().getLocation(), sqwk, template.getAirplaneType(),
        0, Context.getArea().getAirport().getAltitude(), 0, true,
        template.getExitPoint().getNavaid(), template.getExpectedExitTime(), template.getEntryDelay(),
        initialAtcId
    );
    return ret;
  }

  private final AirplaneType airplaneType;
  private final AtcModule atcModule;
  private Coordinate coordinate;
  private final CockpitVoiceRecorder cvr;
  private final DivertModule divertModule;
  private final EmergencyModule emergencyModule;
  private final FlightDataRecorder fdr;
  private final AirplaneFlightModule flightModule;
  private final Mood mood;
  private Pilot pilot;
  private final IAirplane rdr = new AirplaneImpl();
  private final RoutingModule routingModule;
  private final ShaModule sha;
  private final Squawk sqwk;
  private AirplaneState state;
  private final IAirplaneWriter wrt = new AirplaneWriterImpl();

  private Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneType,
                   int heading, int altitude, int speed, boolean isDeparture,
                   Navaid entryExitPoint, EDayTimeStamp expectedExitTime, int entryDelay,
                   AtcId initialAtcId) {


    this.sqwk = sqwk;
    this.flightModule = new AirplaneFlightModule(
        callsign, entryDelay, expectedExitTime, isDeparture);

    this.sha = new ShaModule(this, heading, altitude, speed, airplaneType);
    this.emergencyModule = new EmergencyModule();
    this.atcModule = new AtcModule(this, initialAtcId);
    this.routingModule = new RoutingModule(this, entryExitPoint);
    if (isDeparture)
      this.divertModule = null;
    else
      this.divertModule = new DivertModule(this);
    this.mood = new Mood();
    this.fdr = new FlightDataRecorder(this.flightModule.getCallsign());
    this.cvr = new CockpitVoiceRecorder(this.flightModule.getCallsign());
    this.state = isDeparture ? AirplaneState.holdingPoint : AirplaneState.arrivingHigh;
    this.coordinate = coordinate;
    this.airplaneType = airplaneType;

    if (isDeparture) {
      this.pilot = new HoldingPointPilot(this);
    }
    else {
      this.pilot = new ArrivalPilot(this);
    }
  }

  public void elapseSecond() {

    this.routingModule.elapseSecond();
    this.pilot.elapseSecond();
    this.atcModule.elapseSecond();
    this.divertModule.elapseSecond();

    this.sha.elapseSecond();
    updateCoordinates();

    logToFdr();

    //printAfterCommands();
    //this.recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  public IAirplane getReader() {
    return this.rdr;
  }

  public IAirplaneWriter getWriter() {
    return this.wrt;
  }

  private Navaid getDivertNavaid() {
    IList<DARoute> rts = Context.getArea().getCurrentRunwayConfiguration()
        .getDepartures()
        .where(q -> q.isForCategory(Airplane.this.airplaneType.category))
        .getRandom()
        .getThreshold()
        .getRoutes()
        .where(q -> q.getType() == DARouteType.sid);
    DARoute r = rts.getRandom();
    //TODO here can null-pointer-exception occur when no route is found for threshold and category
    Navaid ret = r.getMainNavaid();
    return ret;
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

  //region Private methods


  private void updateCoordinates() {
    double dist = this.sha.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, this.sha.getHeading(), dist);

    // add wind if flying
    if (this.state.is(
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.landed
    ) == false) {
      Weather weather = Context.getWeather().getWeather();
      newC = Coordinates.getCoordinate(
          newC,
          weather.getWindHeading(),
          UnitProvider.ftToNm(weather.getWindSpeedOrWindGustSpeed()));
    }

    this.coordinate = newC;
  }
//  //endregion


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
//
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
//        Airplane.this.setBehaviorAndState(new ArrivalBehavior(), AirplaneState.arrivingHigh);
//      else
//        Airplane.this.setBehaviorAndState(new DepartureBehavior(), AirplaneState.departingLow);
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
//      if (Airplane.this.state == AirplaneState.holding) {
//        this.abortHolding();
//      }
//
//      NewApproachBehavior behavior = new NewApproachBehavior(newApproachInfo);
//      Airplane.this.setBehaviorAndState(behavior, AirplaneState.flyingIaf2Faf);
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
//      Airplane.this.setBehaviorAndState(new DepartureBehavior(), AirplaneState.departingLow);
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
//          AirplaneState.takeOffGoAround);
//    }
//
//    @Override
//    public void hold(Navaid navaid, int inboundRadial, boolean leftTurn) {
//      HoldBehavior hold = new HoldBehavior(Airplane.this,
//          navaid,
//          inboundRadial,
//          leftTurn);
//      Airplane.this.setBehaviorAndState(hold, AirplaneState.holding);
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
//          AirplaneState.takeOffRoll);
//      Airplane.this.sha.setTargetSpeed(
//          Airplane.this.airplaneType.v2);
//      Airplane.this.sha.setNavigator(
//          new HeadingNavigator(runwayThreshold.getCourse()));
//    }
//


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
}
