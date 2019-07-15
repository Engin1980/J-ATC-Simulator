package eng.jAtcSim.lib.airplanes;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.modules.EmergencyModule;
import eng.jAtcSim.lib.airplanes.modules.MrvaAirproxModule;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.*;
import eng.jAtcSim.lib.messaging.IMessageContent;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Airplane implements IMessageParticipant {

  //region Inner classes

  public class Airplane4Display {

    public Coordinate coordinate() {
      return Airplane.this.coordinate;
    }

    public Callsign callsign() {
      return Airplane.this.flight.getCallsign();
    }

    public Squawk squawk() {
      return Airplane.this.sqwk;
    }

    public Atc tunedAtc() {
      return pilot.getTunedAtc();
    }

    public Atc responsibleAtc() {
      return Acc.prm().getResponsibleAtc(Airplane.this);
    }

    public int heading() {
      return (int) Airplane.this.sha.getHeading();
    }

    public int targetSpeed() {
      return Airplane.this.sha.getTargetSpeed();
    }

    public AirplaneType planeType() {
      return Airplane.this.airplaneType;
    }

    public int verticalSpeed() {
      return (int) Airplane.this.sha.getVerticalSpeed();
    }

    public AirproxType getAirprox() {
      return Airplane.this.mrvaAirproxModule.getAirprox();
    }

    public boolean isDeparture() {
      return Airplane.this.flight.isDeparture();
    }

    public Route getAssignedRoute() {
      return Airplane.this.pilot.getAssignedRoute();
    }

    public ActiveRunwayThreshold getExpectedRunwayThreshold() {
      return Airplane.this.pilot.getExpectedRunwayThreshold();
    }

    public int altitude() {
      return (int) Airplane.this.sha.getAltitude();
    }

    public int targetAltitude() {
      return Airplane.this.sha.getTargetAltitude();
    }

    public int ias() {
      return (int) Airplane.this.sha.getSpeed();
    }

    public double tas() {
      return Airplane.this.getTAS();
    }

    public int targetHeading() {
      return Airplane.this.sha.getTargetHeading();
    }

    public boolean isMrvaError() {
      return Airplane.this.mrvaAirproxModule.isMrvaError();
    }

    public boolean isEmergency() {
      return Airplane.this.emergencyModule.isEmergency();
    }

    public Navaid entryExitPoint() {
      return Airplane.this.getEntryExitFix();
    }

    public boolean hasRadarContact() {
      return Airplane.this.pilot.hasRadarContact();
    }

    public String status() {
      return pilot.getStatusAsString();
    }
  }

  public class Airplane4Pilot {

    public State getState() {
      return state;
    }

    public void setxState(State state) {
      Airplane.this.state = state;
      if (flight.getFinalDelayMinutes() == null) {
        if ((Airplane.this.flight.isArrival() && state == State.landed)
            || (Airplane.this.flight.isDeparture() && state == State.departingLow)) {
          flight.evaluateFinalDelayMinutes();
        }
      }
    }

    public AirplaneType getType() {
      return airplaneType;
    }

    public ShaModule getSha(){
      return Airplane.this.sha;
    }

    public Coordinate getCoordinate() {
      return coordinate;
    }

    public void adviceGoAroundToAtc(Atc targetAtc, GoingAroundNotification.GoAroundReason reason) {
      IAirplaneNotification notification = new GoingAroundNotification(reason);
      adviceToAtc(targetAtc, notification);
    }

    public void adviceToAtc(Atc targetAtc, IAirplaneNotification notification) {
      SpeechList lst = new SpeechList(notification);
      Message m = new Message(Airplane.this, targetAtc,
          lst);
      Acc.messenger().send(m);
    }

    public boolean isArrival() {
      return flight.isArrival();
    }

//    public Airplane getMe() {
//      return Airplane.this;
//    }

    public Callsign getCallsign() {
      return flight.getCallsign();
    }

    public void passMessageToAtc(Atc atc, SpeechList saidText) {
      Message m = new Message(Airplane.this, atc, saidText);
      Acc.messenger().send(m);
    }

    public void passMessageToAtc(Atc atc, IFromAirplane content) {
      SpeechList saidText = new SpeechList();
      saidText.add(content);
      passMessageToAtc(atc, saidText);
    }

    public void divert() {
      Airplane.this.flight.divert();
    }

    public boolean isEmergency() {
      return Airplane.this.emergencyModule.isEmergency();
    }

    public AirproxType getAirprox() {
      return Airplane.this.mrvaAirproxModule.getAirprox();
    }

    public void evaluateMoodForShortcut(Navaid navaid) {
      Route r = getAssigneRoute();
      if (r == null) return;
      if (r.getNavaids().isEmpty()) return;
      if (r.getNavaids().getLast().equals(navaid)) {
        if (Airplane.this.flight.isArrival()) {
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

    public Mood getMood() {
      return Airplane.this.mood;
    }

    public void setNavigator(INavigator navigator) {
      Airplane.this.sha.setNavigator(navigator);
    }
  }

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

  public class AdvancedReader {

    public int getTargetAltitude() {
      return Airplane.this.sha.getTargetAltitude();
    }

    public String getHeadingS() {
      return String.format("%1$03d", (int) Airplane.this.getHeading());
    }

    public Coordinate getCoordinate() {
      return coordinate;
    }

    public int getTargetHeading() {
      return Airplane.this.sha.getTargetHeading();
    }

    public String getTargetHeadingS() {
      return String.format("%1$03d", getTargetHeading());
    }

    public Navaid getDepartureLastNavaid() {
      if (Airplane.this.flight.isDeparture() == false)
        throw new EApplicationException(sf(
            "This method should not be called on departure aircraft %s.",
            Airplane.this.flight.getCallsign().toString()));

      Navaid ret = Airplane.this.pilot.getAssignedRoute().getMainNavaid();
      return ret;
    }

    public boolean isOnWayToPassDeparturePoint() {
      Navaid n = this.getDepartureLastNavaid();
      boolean ret = Airplane.this.pilot.isOnWayToPassPoint(n);
      return ret;
    }

    public double getTargetSpeed() {
      return Airplane.this.sha.getTargetSpeed();
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

    public boolean isOnGround() {
      return this == takeOffRoll || this == landed || this == holdingPoint;
    }

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
//    ret.flightRecorder = FlightRecorder.create(ret.flight.getCallsign());
//
//    XElement tmp = elm.getChildren().getFirst(q -> q.getName().equals("pilot"));
//
//    ret.pilot = Pilot.load(tmp, ret.new Airplane4Pilot());
//
//    return ret;
  }


  private final AirplaneType airplaneType;

  private final Squawk sqwk;

  private final AirplaneFlight flight;
  private final Pilot pilot;
  private Coordinate coordinate;
  private State state;

  private final ShaModule sha = new ShaModule(this);
  private final EmergencyModule emergencyModule = new EmergencyModule(this);
  private final MrvaAirproxModule mrvaAirproxModule = new MrvaAirproxModule(this);
  private Mood mood;
  private final AdvancedReader advancedReader = new AdvancedReader();

  @XmlIgnore
  private FlightRecorder flightRecorder = null;
  @XmlIgnore
  private final Airplane4Display plane4Display = new Airplane4Display();


  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
                  int heading, int altitude, int speed, boolean isDeparture,
                  Navaid entryExitPoint, int delayInitialMinutes, ETime delayExpectedTime) {

    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneType = airplaneSpecification;
    this.state = isDeparture ? State.holdingPoint : State.arrivingHigh;

    this.flight = new AirplaneFlight(callsign, delayInitialMinutes, delayExpectedTime, isDeparture);
    this.sha.init(heading, altitude, speed, airplaneSpecification, Acc.airport().getAltitude());
    this.pilot = new Pilot(this.new Airplane4Pilot(), entryExitPoint, generateDivertTime(isDeparture));
    this.flightRecorder = FlightRecorder.create(callsign);
    this.mood = new Mood();
  }

  @XmlConstructor
  private Airplane() {
    this.sqwk = null;
    this.airplaneType = null;
    this.flight = new AirplaneFlight(null, 0, null, false);
    this.pilot = new Pilot(this.new Airplane4Pilot(), null, null);
    this.mood = null;
  }

  public AirplaneFlight getFlight() {
    return this.flight;
  }

  public Coordinate getCoordinate() {
    return this.coordinate;
  }

  public double getAltitude() {
    return this.sha.getAltitude();
  }

  public AirplaneType getType() {
    return this.airplaneType;
  }

  public Pilot getPilot() {
    return this.pilot;
  }

  public AdvancedReader getAdvanced() {
    return this.advancedReader;
  }

  public double getSpeed() {
    return this.sha.getSpeed();
  }

  public Atc getTunedAtc() {
    return pilot.getTunedAtc();
  }

  public double getTAS() {
    double m = 1 + this.sha.getAltitude() / 100000d;
    double ret = this.sha.getSpeed() * m;
    return ret;
  }

  public double getGS() {
    return getTAS();
  }

  public double getVerticalSpeed() {
    return this.sha.getVerticalSpeed();
  }

  public State getState() {
    return state;
  }

  public double getHeading() {
    return this.sha.getHeading();
  }

  public Squawk getSqwk() {
    return sqwk;
  }

  public FlightRecorder getFlightRecorder() {
    return flightRecorder;
  }

  public void elapseSecond() {

    processMessages();
    drivePlane();
    this.sha.elapseSecond();
    updateCoordinates();

    flightRecorder.logFDR(this, this.pilot);
  }

  public MrvaAirproxModule getMrvaAirproxModule() {
    return mrvaAirproxModule;
  }

  @Override
  public String toString() {
    return this.flight.getCallsign().toString();
  }

  @Override
  public String getName() {
    return this.flight.getCallsign().toString();
  }

  public Route getAssigneRoute() {
    return this.pilot.getAssignedRoute();
  }

  public Airplane4Display getPlane4Display() {
    return this.plane4Display;
  }

  public ActiveRunwayThreshold tryGetCurrentApproachRunwayThreshold() {
    NewApproachInfo app = this.pilot.tryGetAssignedApproach();
    ActiveRunwayThreshold ret;
    if (app == null)
      ret = null;
    else
      ret = app.getThreshold();
    return ret;
  }

  public ShaModule getSha() {
    return this.sha;
  }

  public void setHoldingPointState(Coordinate coordinate, double course) {
    assert this.state == State.holdingPoint;
    this.coordinate = coordinate;
    this.sha.setTargetHeading((int) Math.round(course));
  }

  public void updateAssignedRouting(Route route, ActiveRunwayThreshold expectedRunwayThreshold) {
    pilot.updateAssignedRouting(route, expectedRunwayThreshold);
  }


  public ActiveRunwayThreshold getAssignedRunwayThresholdForLanding() {
    ActiveRunwayThreshold ret = tryGetAssignedRunwayThresholdForLanding();
    if (ret == null) {
      throw new EApplicationException(this.getFlight().getCallsign().toString() + " has no assigned departure/arrival threshold.");
    }
    return ret;
  }

  public ActiveRunwayThreshold tryGetAssignedRunwayThresholdForLanding() {
    ActiveRunwayThreshold ret;
    NewApproachInfo cai = pilot.tryGetAssignedApproach();
    if (cai == null) {
      ret = null;
    } else {
      ret = cai.getThreshold();
    }
    return ret;
  }

  public void save(XElement elm) {
    LoadSave.saveField(elm, this, "callsign");
    LoadSave.saveField(elm, this, "sqwk");
    LoadSave.saveField(elm, this, "airplaneType");
    LoadSave.saveField(elm, this, "delayInitialMinutes");
    LoadSave.saveField(elm, this, "delayExpectedTime");
    LoadSave.saveField(elm, this, "departure");
    LoadSave.saveField(elm, this, "targetHeading");
    LoadSave.saveField(elm, this, "targetHeadingLeftTurn");
    LoadSave.saveField(elm, this, "targetAltitude");
    LoadSave.saveField(elm, this, "targetSpeed");
    LoadSave.saveField(elm, this, "state");
    LoadSave.saveField(elm, this, "lastVerticalSpeed");
    LoadSave.saveField(elm, this, "airprox");
    LoadSave.saveField(elm, this, "mrvaError");
    LoadSave.saveField(elm, this, "delayResult");
    LoadSave.saveField(elm, this, "emergencyWanishTime");
    LoadSave.saveField(elm, this, "coordinate");
    LoadSave.saveField(elm, this, "heading");
    LoadSave.saveField(elm, this, "speed");
    LoadSave.saveField(elm, this, "altitude");
    LoadSave.saveField(elm, this, "mood");

    XElement tmp = new XElement("pilot");
    this.pilot.save(tmp);
    elm.addElement(tmp);

  }


  public Navaid getEntryExitFix() {
    return pilot.getEntryExitPoint();
  }

  public Mood getMood() {
    return this.mood;
  }

  public MoodResult getEvaluatedMood() {
    MoodResult ret = this.mood.evaluate(this.flight.getCallsign(), this.flight.getFinalDelayMinutes());
    return ret;
  }

  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return pilot.getExpectedRunwayThreshold();
  }

  private static ETime generateDivertTime(boolean isDeparture) {
    ETime divertTime = null;
    if (!isDeparture) {
      int divertTimeMinutes = Acc.rnd().nextInt(MINIMAL_DIVERT_TIME_MINUTES, MAXIMAL_DIVERT_TIME_MINUTES);
      divertTime = Acc.now().addMinutes(divertTimeMinutes);
    }
    return divertTime;
  }

  //region Private methods
  private void drivePlane() {
    pilot.elapseSecond();
  }

  private void processMessages() {
    IList<Message> msgs = Acc.messenger().getMessagesByListener(this, true);
    for (Message m : msgs) {
      processMessage(m);
    }
  }

  private void processMessage(Message msg) {
    // if item from non-tuned ATC, then is ignored
    if (msg.getSource() != this.pilot.getTunedAtc()) {
      return;
    }

    SpeechList cmds;
    IMessageContent s = msg.getContent();
    if (isValidMessageForAirplane(s)) {
      if (s instanceof SpeechList)
        cmds = (SpeechList) s;
      else {
        cmds = new SpeechList();
        cmds.add((ISpeech) s);
      }
    } else {
      throw new EApplicationException("Airplane can only deal with messages containing \"IFromAtc\" or \"List<IFromAtc>\".");
    }

    processCommands(cmds);
  }

  private boolean isValidMessageForAirplane(IMessageContent msg) {
    if (msg instanceof IFromAtc)
      return true;
    else if (msg instanceof SpeechList) {
      for (ISpeech o : (SpeechList<ISpeech>) msg) {
        if (!(o instanceof IFromAtc)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void processCommands(SpeechList speeches) {
    this.pilot.addNewSpeeches(speeches);
  }

  private void updateCoordinates() {
    double dist = this.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, this.sha.getHeading(), dist);

    // add wind if flying
    if (this.getState().is(
        State.holdingPoint,
        State.takeOffRoll,
        State.landed
    ) == false)
      newC = Coordinates.getCoordinate(
          newC,
          Acc.weather().getWindHeading(),
          UnitProvider.ftToNm(Acc.weather().getWindSpeedOrWindGustSpeed()));

    this.coordinate = newC;
  }

  //endregion
}
