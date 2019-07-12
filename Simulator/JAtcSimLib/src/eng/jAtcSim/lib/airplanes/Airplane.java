package eng.jAtcSim.lib.airplanes;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.HeadingsNew;
import eng.jAtcSim.lib.global.UnitProvider;
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
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.newApproaches.Approach;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

import java.util.ArrayList;
import java.util.List;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Airplane implements IMessageParticipant {

  public class Airplane4Display {

    public Coordinate coordinate() {
      return Airplane.this.coordinate;
    }

    public Callsign callsign() {
      return Airplane.this.callsign;
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
      return (int) Airplane.this.heading.getValue();
    }

    public int targetSpeed() {
      return Airplane.this.targetSpeed;
    }

    public AirplaneType planeType() {
      return Airplane.this.airplaneType;
    }

    public int verticalSpeed() {
      return (int) Airplane.this.lastVerticalSpeed;
    }

    public AirproxType getAirprox() {
      return airprox;
    }

    public boolean isDeparture() {
      return Airplane.this.departure;
    }

    public Route getAssignedRoute() {
      return Airplane.this.pilot.getAssignedRoute();
    }

    public ActiveRunwayThreshold getExpectedRunwayThreshold() {
      return Airplane.this.pilot.getExpectedRunwayThreshold();
    }

    public int altitude() {
      return (int) Airplane.this.altitude.getValue();
    }

    public int targetAltitude() {
      return (int) Airplane.this.targetAltitude;
    }

    public int ias() {
      return (int) Airplane.this.speed.getValue();
    }

    public double tas() {
      return Airplane.this.getTAS();
    }

    public int targetHeading() {
      return (int) Airplane.this.targetHeading;
    }

    public boolean isMrvaError() {
      return Airplane.this.mrvaError;
    }

    public boolean isEmergency() {
      return Airplane.this.isEmergency();
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
      if (delayResult == null) {
        if ((Airplane.this.isArrival() && state == State.landed)
            || (Airplane.this.isDeparture() && state == State.departingLow)) {
          Airplane.this.delayResult = Acc.now().getTotalMinutes() - Airplane.this.delayExpectedTime.getTotalMinutes();
        }
      }
    }

    public AirplaneType getType() {
      return airplaneType;
    }

    public double getSpeed() {
      return speed.getValue();
    }

    public Coordinate getCoordinate() {
      return coordinate;
    }

    public double getAltitude() {
      return altitude.getValue();
    }

    public double getTargetHeading() {
      return targetHeading;
    }

    public void setTargetHeading(double value) {
      Airplane.this.setTargetHeading(value);
    }

    public void setTargetHeading(double value, boolean useLeftTurn) {
      Airplane.this.setTargetHeading(value, useLeftTurn);
    }

    public double getHeading() {
      return heading.getValue();
    }

    public int getTargetAltitude() {
      return targetAltitude;
    }

    public void setTargetAltitude(int altitudeInFt) {
      Airplane.this.setTargetAltitude(altitudeInFt);
    }

    public int getTargetSpeed() {
      return targetSpeed;
    }

    public void setTargetSpeed(int speed) {
      Airplane.this.setTargetSpeed(speed);
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
      return !departure;
    }

    public Airplane getMe() {
      return Airplane.this;
    }

    public Callsign getCallsign() {
      return callsign;
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

    public Airplane4Command getPlane4Command() {
      return Airplane.this.new Airplane4Command();
    }

    public void divert() {
      Airplane.this.departure = true;
    }

    public boolean isEmergency() {
      return Airplane.this.isEmergency();
    }

    public AirproxType getAirprox() {
      return Airplane.this.airprox;
    }

    public void evaluateMoodForShortcut(Navaid navaid) {
      Route r = getAssigneRoute();
      if (r == null) return;
      if (r.getNavaids().isEmpty()) return;
      if (r.getNavaids().getLast().equals(navaid)) {
        if (Airplane.this.isArrival()) {
          if (Airplane.this.altitude.value > 1e4)
            mood.experience(Mood.ArrivalExperience.shortcutToIafAbove100);
        } else {
          if (Airplane.this.altitude.value > 1e4)
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
      Airplane.this.navigator = navigator;
    }
  }

  public class Airplane4Command {

    public boolean isEmergency() {
      return Airplane.this.isEmergency();
    }

    public State getState() {
      return state;
    }

    public Pilot.Pilot4Command getPilot() {
      return pilot.pilot4Command;
    }

    public Coordinate getCoordinate() {
      return coordinate;
    }

    public AirplaneType getType() {
      return airplaneType;
    }

    public double getAltitude() {
      return altitude.getValue();
    }

    public int getTargetAltitude() {
      return targetAltitude;
    }

    public double getHeading() {
      return heading.getValue();
    }

    public Callsign getCallsign() {
      return callsign;
    }

    public void setTakeOffPosition(Coordinate coordinate) {
      Airplane.this.coordinate = coordinate;
    }

    public boolean isArrival() {
      return Airplane.this.isArrival();
    }

  }

  public class Airplane4Navigator {
    public int getTargetHeading() {
      return Airplane.this.getTargetHeading();
    }

    public void setTargetHeading(int heading) {
      Airplane.this.setTargetHeading(heading);
    }

    public Coordinate getCoordinates() {
      return Airplane.this.coordinate;
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
      for (int i = 0; i < values.length; i++) {
        if (this == values[i]) {
          ret = true;
          break;
        }
      }
      return ret;
    }
  }

  private static final int MINIMAL_DIVERT_TIME_MINUTES = 45;
  private static final int MAXIMAL_DIVERT_TIME_MINUTES = 120;
  private final static double GROUND_SPEED_CHANGE_MULTIPLIER = 1.5; //1.5; //3.0;
  private static final double secondFraction = 1 / 60d / 60d;

  public static Airplane load(XElement elm) {

    Airplane ret = new Airplane();

    LoadSave.loadField(elm, ret, "callsign");
    LoadSave.loadField(elm, ret, "sqwk");
    LoadSave.loadField(elm, ret, "airplaneType");
    LoadSave.loadField(elm, ret, "delayInitialMinutes");
    LoadSave.loadField(elm, ret, "delayExpectedTime");
    LoadSave.loadField(elm, ret, "departure");
    LoadSave.loadField(elm, ret, "targetHeading");
    LoadSave.loadField(elm, ret, "targetHeadingLeftTurn");
    LoadSave.loadField(elm, ret, "targetAltitude");
    LoadSave.loadField(elm, ret, "targetSpeed");
    LoadSave.loadField(elm, ret, "state");
    LoadSave.loadField(elm, ret, "lastVerticalSpeed");
    LoadSave.loadField(elm, ret, "airprox");
    LoadSave.loadField(elm, ret, "mrvaError");
    LoadSave.loadField(elm, ret, "delayResult");
    LoadSave.loadField(elm, ret, "emergencyWanishTime");
    LoadSave.loadField(elm, ret, "coordinate");
    LoadSave.loadField(elm, ret, "heading");
    LoadSave.loadField(elm, ret, "speed");
    LoadSave.loadField(elm, ret, "altitude");
    LoadSave.loadField(elm, ret, "mood");

    ret.flightRecorder = FlightRecorder.create(ret.callsign);

    XElement tmp = elm.getChildren().getFirst(q -> q.getName().equals("pilot"));

    ret.pilot = Pilot.load(tmp, ret.new Airplane4Pilot());

    return ret;
  }

  private static ValueRequest getRequest(double current, double target, double maxIncreaseStep, double maxDecreaseStep) {
    // if on ground, nothing required
    final double RUN_OUT_COEFF = 0.2;
    final double RUN_OUT_DISTANCE = 3;

    double delta = target - current;
    if (delta == 0) {
      return new ValueRequest();
      // no change required
    }

    double absDelta = delta;
    double availableStep;
    if (delta > 0) {
      // needs to accelerate
      availableStep = maxIncreaseStep;
    } else {
      availableStep = maxDecreaseStep;
      absDelta = -delta;
    }

    ValueRequest ret = new ValueRequest();
    double deltaPress = absDelta / availableStep;
    if (deltaPress > RUN_OUT_DISTANCE) {
      ret.value = availableStep;
      ret.energy = 1;
    } else if (deltaPress > 1) {
      ret.value = availableStep * RUN_OUT_COEFF;
      ret.energy = RUN_OUT_COEFF;
    } else {
      absDelta = Math.min(absDelta, availableStep * RUN_OUT_COEFF);
      ret.value = absDelta;
      ret.energy = absDelta / availableStep;
    }

    if (delta < 0)
      ret.multiply(-1);

    return ret;
  }
  private final AirplaneType airplaneType;
  @XmlIgnore
  private final Airplane4Display plane4Display;
  @XmlIgnore
  private final Airplane4Navigator plane4Navigator;
  private final Squawk sqwk;
  private final SHA sha;
  private final AirplaneFlight flight;
  private final Pilot pilot;
  private Coordinate coordinate;
  private State state;
  @XmlIgnore
  private FlightRecorder flightRecorder = null;
  private AirproxType airprox;
  private boolean mrvaError;
  private Integer delayResult = null;
  private ETime emergencyWanishTime = null;
  private Mood mood;

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
                  int heading, int altitude, int speed, boolean isDeparture,
                  Navaid entryExitPoint, int delayInitialMinutes, ETime delayExpectedTime) {

    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneType = airplaneSpecification;
    this.state = isDeparture ? State.holdingPoint : State.arrivingHigh;

    this.flight = new AirplaneFlight(callsign, delayInitialMinutes, delayExpectedTime, isDeparture);
    this.sha = new SHA(heading, altitude, speed, airplaneSpecification, Acc.airport().getAltitude());
    this.pilot = new Pilot(this.new Airplane4Pilot(), entryExitPoint, generateDivertTime(isDeparture));
    this.flightRecorder = FlightRecorder.create(callsign);
    this.mood = new Mood();

    this.plane4Display = this.new Airplane4Display();
    this.plane4Navigator = this.new Airplane4Navigator();
  }

  @XmlConstructor
  private Airplane() {
    this.sqwk = null;
    this.airplaneType = null;
    this.flight = new AirplaneFlight(null, 0, null, false);
    this.pilot = new Pilot(this.new Airplane4Pilot(), null, null);
    this.sha = new SHA(0, 0, 0, null, 0);
    this.plane4Display = new Airplane4Display();
    this.plane4Navigator = new Airplane4Navigator();
    this.mood = null;
  }

  public boolean isEmergency() {
    return this.emergencyWanishTime != null;
  }

  public boolean isDeparture() {
    return departure;
  }

  public boolean isArrival() {
    return !departure;
  }

  public double getVerticalSpeed() {
    return lastVerticalSpeed;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public State getState() {
    return state;
  }

  public double getHeading() {
    return heading.getValue();
  }

  public String getHeadingS() {
    return String.format("%1$03d", (int) this.heading.getValue());
  }

  public double getAltitude() {
    return altitude.getValue();
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Squawk getSqwk() {
    return sqwk;
  }

  public AirplaneType getType() {
    return airplaneType;
  }

  public double getSpeed() {
    return speed.getValue();
  }

  public FlightRecorder getFlightRecorder() {
    return flightRecorder;
  }

  public String getTargetHeadingS() {
    return String.format("%1$03d", this.targetHeading);
  }

  public void elapseSecond() {

    processMessages();
    drivePlane();
    updateSHABySecondNew();
    updateCoordinates();

    flightRecorder.logFDR(this, this.pilot);
  }

  @Override
  public String toString() {
    return this.flight.getCallsign().toString();
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

  public void setTargetHeading(double targetHeading) {
    this.setTargetHeading((int) Math.round(targetHeading));
  }

  @Override
  public String getName() {
    return this.getCallsign().toString();
  }

  public Navaid getDepartureLastNavaid() {
    if (isDeparture() == false)
      throw new EApplicationException(sf(
          "This method should not be called on departure aircraft %s.",
          this.getCallsign().toString()));

    Navaid ret = this.pilot.getAssignedRoute().getMainNavaid();
    return ret;
  }

  public AirproxType getAirprox() {
    return this.airprox;
  }

  public void setAirprox(AirproxType airprox) {
    this.airprox = airprox;
  }

  public Route getAssigneRoute() {
    return this.pilot.getAssignedRoute();
  }

  public Airplane4Display getPlane4Display() {
    return this.plane4Display;
  }

  public boolean isOnWayToPassDeparturePoint() {
    Navaid n = this.getDepartureLastNavaid();
    boolean ret = this.pilot.isOnWayToPassPoint(n);
    return ret;
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

  public void setHoldingPointState(Coordinate coordinate, double course) {
    assert this.state == State.holdingPoint;
    this.coordinate = coordinate;
    this.sha.setTargetHeading((int) Math.round(course));
  }

  public boolean isMrvaError() {
    return mrvaError;
  }

  public void setMrvaError(boolean mrvaError) {
    this.mrvaError = mrvaError;
  }

  public int getDelayDifference() {
    return delayResult;
  }

  public void updateAssignedRouting(Route route, ActiveRunwayThreshold expectedRunwayThreshold) {
    pilot.updateAssignedRouting(route, expectedRunwayThreshold);
  }

  public void raiseEmergency() {
    int minsE = Acc.rnd().nextInt(5, 60);
    double distToAip = Coordinates.getDistanceInNM(this.coordinate, Acc.airport().getLocation());
    int minA = (int) (distToAip / 250d * 60);
    ETime wt = Acc.now().addMinutes(minsE + minA);

    int alt = Math.max((int) this.getAltitude(), Acc.airport().getAltitude() + 4000);
    alt = (int) NumberUtils.ceil(alt, 3);
    this.sha.setTargetAltitude(alt);

    this.emergencyWanishTime = wt;
    this.departure = false;
    this.pilot.raiseEmergency();
  }

  public boolean hasElapsedEmergencyTime() {

    assert this.emergencyWanishTime != null;
    boolean ret = this.emergencyWanishTime.isBefore(Acc.now());
    return ret;
  }

  public ActiveRunwayThreshold getAssignedRunwayThresholdForLanding() {
    ActiveRunwayThreshold ret = tryGetAssignedRunwayThresholdForLanding();
    if (ret == null) {
      throw new EApplicationException(this.getCallsign().toString() + " has no assigned departure/arrival threshold.");
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
    /*
  private FlightRecorder flightRecorder = null;
     */
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

  public void resetAirprox() {
    this.airprox = AirproxType.none;
  }

  public void increaseAirprox(AirproxType at) {
    this.airprox = AirproxType.combine(this.airprox, at);
  }

  public Navaid getEntryExitFix() {
    return pilot.getEntryExitPoint();
  }

  public Mood getMood() {
    return this.mood;
  }

  public MoodResult getEvaluatedMood() {
    MoodResult ret = this.mood.evaluate(this.flight.getCallsign(), this.delayResult);
    return ret;
  }

  public ActiveRunwayThreshold getExpectedRunwayThreshold() {
    return pilot.getExpectedRunwayThreshold();
  }

  private ETime generateDivertTime(boolean isDeparture) {
    ETime divertTime = null;
    if (!isDeparture) {
      int divertTimeMinutes = Acc.rnd().nextInt(MINIMAL_DIVERT_TIME_MINUTES, MAXIMAL_DIVERT_TIME_MINUTES);
      divertTime = Acc.now().addMinutes(divertTimeMinutes);
    }
    return divertTime;
  }

  // <editor-fold defaultstate="collapsed" desc=" private methods ">
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

  private void updateSHABySecondNew() {
    // TODO here is && or || ???
    boolean isSpeedPreffered =
        this.state == State.takeOffGoAround || this.state == State.takeOffGoAround;

    if (targetAltitude != altitude.getValue() || targetSpeed != speed.getValue()) {

      ValueRequest speedRequest = getSpeedRequest();
      ValueRequest altitudeRequest = getAltitudeRequest();

      double totalEnergy = Math.abs(speedRequest.energy + altitudeRequest.energy);
      if (totalEnergy > 1) {
        if (!isSpeedPreffered) {
          double energyMultiplier = 1 / totalEnergy;
          speedRequest.multiply(energyMultiplier);
          altitudeRequest.multiply(energyMultiplier);
        } else {
          // when speed is preferred
          double energyLeft = 1 - speedRequest.energy;
          altitudeRequest.multiply(energyLeft);
        }
      }

      adjustSpeed(speedRequest);
      adjustAltitude(altitudeRequest);

    } else if (this.lastVerticalSpeed != 0)
      this.lastVerticalSpeed = 0;

    this.navigator.navigate(this.plane4Navigator);
    if (targetHeading != heading.getValue()) {
      adjustHeading();
    } else {
      this.heading.resetInertia();
    }
  }

  private ValueRequest getSpeedRequest() {
    ValueRequest ret;
    double delta = targetSpeed - speed.getValue();
    if (delta == 0) {
      // no change required
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double incStep = airplaneType.speedIncreaseRate;
      double decStep = airplaneType.speedDecreaseRate;
      if (this.state.isOnGround()) {
        incStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
        decStep *= GROUND_SPEED_CHANGE_MULTIPLIER;
      }
      ret = getRequest(
          this.speed.getValue(),
          this.targetSpeed,
          incStep, decStep);
    }

    return ret;
  }

  private ValueRequest getAltitudeRequest() {
    ValueRequest ret;
    // if on ground, nothing required
    if (this.state.isOnGround() && altitude.getValue() == Acc.airport().getAltitude()) {
      ret = new ValueRequest();
      ret.energy = 0;
      ret.value = 0;
    } else {
      double climbRateForAltitude = airplaneType.getClimbRateForAltitude(this.altitude.getValue());
      double descentRateForAltitude = airplaneType.getDescendRateForAltitude(this.altitude.getValue());
      descentRateForAltitude = adjustDescentRateByApproachStateIfRequired(descentRateForAltitude);
      ret = getRequest(
          this.altitude.getValue(),
          this.targetAltitude,
          climbRateForAltitude,
          descentRateForAltitude);
    }

    return ret;
  }

  private double adjustDescentRateByApproachStateIfRequired(double descentRateForAltitude) {
    double ret;
    if (state.is(State.approachDescend, State.longFinal, State.shortFinal)) {
      double restrictedDescentRate;
      switch (state) {
        case approachDescend:
          restrictedDescentRate = 2000;
          break;
        case longFinal:
          restrictedDescentRate = this.pilot.tryGetAssignedApproach().getApproach().getType() == Approach.ApproachType.visual ?
              2000 : 1300;
          break;
        case shortFinal:
          restrictedDescentRate = 1300;
          break;
        default:
          throw new UnsupportedOperationException("This situation is not supported.");
      }
      restrictedDescentRate /= 60d;
      ret = Math.min(descentRateForAltitude, restrictedDescentRate);
    } else
      ret = descentRateForAltitude;
    return ret;
  }

  private void adjustSpeed(ValueRequest speedRequest) {
    this.speed.add(speedRequest.value);
  }

  private void adjustAltitude(ValueRequest altitudeRequest) {
    if (this.getState().is(State.takeOffRoll, State.landed, State.holdingPoint)) {
      // not adjusting altitude at this states
      this.altitude.reset(Acc.airport().getAltitude());
    } else {
      this.altitude.add(altitudeRequest.value);
      this.lastVerticalSpeed = this.altitude.getInertia() * 60;
      if (this.altitude.getValue() < Acc.airport().getAltitude()) {
        this.altitude.reset(Acc.airport().getAltitude());
      }
    }
  }

  private void adjustHeading() {
    double diff = Headings.getDifference(heading.getValue(), targetHeading, true);

    boolean isLeft = targetHeadingLeftTurn;
    if (diff < 5)
      isLeft = HeadingsNew.getBetterDirectionToTurn(heading.getValue(), targetHeading) == ChangeHeadingCommand.eDirection.left;

    if (isLeft)
      this.heading.add(-diff);
    else
      this.heading.add(diff);
  }

  private void updateCoordinates() {
    double dist = this.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, heading.getValue(), dist);

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

  // </editor-fold>
}

class ValueRequest {
  public double value;
  public double energy;

  public void multiply(double multiplier) {
    this.value *= multiplier;
    this.energy *= multiplier;
  }

  @Override
  public String toString() {
    return "ValueRequest{" +
        "value=" + value +
        ", energy=" + energy +
        '}';
  }
}

class InertialValue {
  private final double maxPositiveInertiaChange;
  private final double maxNegativeInertiaChange;
  protected double value;
  private double inertia;
  private Double minimum;

  @XmlConstructor
  private InertialValue() {
    maxPositiveInertiaChange = Double.MIN_VALUE;
    maxNegativeInertiaChange = Double.MIN_VALUE;
  }

  public InertialValue(double value,
                       double maxPositiveInertiaChange, double maxNegativeInertiaChange,
                       @Nullable Double minimum) {
    this.value = value;
    this.inertia = 0;
    this.minimum = minimum;
    this.maxPositiveInertiaChange = maxPositiveInertiaChange;
    this.maxNegativeInertiaChange = maxNegativeInertiaChange;
  }

  public void reset(double value) {
    this.value = value;
    this.inertia = 0;
  }

  public void add(double val) {
    double adjustedValue;
    if (val > inertia)
      adjustedValue = Math.min(val, inertia + maxPositiveInertiaChange);
    else
      adjustedValue = Math.max(val, inertia - maxNegativeInertiaChange);

    this.inertia = adjustedValue;
    this.value += this.inertia;

    if ((this.minimum != null) && (this.value < this.minimum)) {
      this.value = this.minimum;
      this.inertia = 0;
    }
  }

  public void set(double value) {
    double diff = value - this.value;
    this.add(diff);
  }

  public double getValue() {
    return value;
  }

  public double getInertia() {
    return inertia;
  }

  public double getMaxPositiveInertiaChange() {
    return maxPositiveInertiaChange;
  }

  public double getMaxNegativeInertiaChange() {
    return maxNegativeInertiaChange;
  }
}

class HeadingInertialValue {
  private final double maxInertia;
  private final double maxInertiaChange;
  protected double value;
  private List<Double> thresholds = new ArrayList();
  private int inertiaStep = 0;

  public HeadingInertialValue(double value,
                              double maxInertia, double maxInertiaChange) {
    this.value = value;
    this.maxInertia = maxInertia;
    this.maxInertiaChange = maxInertiaChange;
    buildHashMap();
  }

  @XmlConstructor
  private HeadingInertialValue() {
    maxInertia = Double.MIN_VALUE;
    maxInertiaChange = Double.MIN_VALUE;
  }

  public void reset(double value) {
    this.value = value;
    this.inertiaStep = 0;
  }

  public void add(double val) {
    if (Math.abs(val) < maxInertiaChange) {
      this.value += val;
      this.inertiaStep = 0;
    } else {
      int stepBlock = getFromHashMap(val);
      if (stepBlock < inertiaStep)
        inertiaStep--;
      else if (stepBlock > inertiaStep)
        inertiaStep++;

      double step = inertiaStep * maxInertiaChange;
      step = Math.min(step, this.maxInertia);
      if (val > 0)
        step = Math.min(step, val);
      else
        step = Math.max(step, val);

      this.value += step;
    }

    this.value = Headings.to(this.value);
  }

  public double getValue() {
    return value;
  }

  public double getInertia() {
    return inertiaStep * maxInertiaChange;
  }

  public double getMaxInertia() {
    return maxInertia;
  }

  public void resetInertia() {
    if (this.inertiaStep != 0)
      this.inertiaStep = 0;
  }

  private void buildHashMap() {
    List<Double> tmp = new ArrayList<>();
    int index = 1;
    int cumIndex = 1;
    double maxThr = maxInertia / maxInertiaChange + 1;
    double thr = 0;
    while (thr <= maxThr) {
      thr = cumIndex * this.maxInertiaChange;
      tmp.add(thr);
      index++;
      cumIndex += index;
    }

    tmp.remove(0);

    this.thresholds = tmp;
  }

  private int getFromHashMap(double val) {
    boolean isNeg = false;
    if (val < 0) {
      isNeg = true;
      val = -val;
    }
    int ret = 0;
    while (ret < this.thresholds.size()) {
      if (val >= this.thresholds.get(ret))
        ret++;
      else
        break;
    }
    ret = ret + 1;
    if (isNeg)
      ret = -ret;
    return ret;
  }
}