/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import com.sun.istack.internal.Nullable;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.UnitProvider;
import eng.jAtcSim.lib.messaging.IMessageContent;
import eng.jAtcSim.lib.messaging.IMessageParticipant;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.world.Navaid;

import java.util.List;

/**
 * @author Marek
 */
public class Airplane implements KeyItem<Callsign>, IMessageParticipant {

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
      return (int) Airplane.this.heading;
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

    public boolean isAirprox() {
      return airprox;
    }

    public boolean isDeparture() {
      return Airplane.this.departure;
    }

    public String routeNameOrFix() {
      return Airplane.this.pilot.getRouteName();
    }

    public int altitude() {
      return (int) Airplane.this.altitude.getValue();
    }

    public int targetAltitude() {
      return (int) Airplane.this.targetAltitude;
    }

    public int speed() {
      return (int) Airplane.this.speed.getValue();
    }

    public int targetHeading() {
      return (int) Airplane.this.targetHeading;
    }
  }

  public class Airplane4Pilot {
    public State getState() {
      return state;
    }

    public void setxState(State state) {
      Airplane.this.state = state;
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
      return heading;
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

    public void adviceGoAroundToAtc(Atc targetAtc, String reason) {
      IAirplaneNotification notification = new GoingAroundNotification(reason);
      adviceToAtc(targetAtc, notification);
    }

    public void adviceToAtc(Atc targetAtc, IAirplaneNotification notification) {
      Message m = new Message(Airplane.this, targetAtc,
          notification);
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
      Message message = new Message(
          Airplane.this, atc,
          content);
      Acc.messenger().send(message);
    }

    public Airplane4Command getPlane4Command() {
      return Airplane.this.new Airplane4Command();
    }
  }

  public class Airplane4Command {

    public State getState() {
      return state;
    }

    public Pilot.Pilot4Command getPilot() {
      return pilot.new Pilot4Command();
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

    public void setTargetAltitude(int targetAltitude) {
      Airplane.this.targetAltitude = targetAltitude;
    }

    public double getHeading() {
      return heading;
    }

    public void setTargetHeading(double value, boolean useLeftTurn) {
      Airplane.this.setTargetHeading(value, useLeftTurn);
    }

    public Callsign getCallsign() {
      return callsign;
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

  private final static double GROUND_SPEED_CHANGE_MULTIPLIER = 1.5; //1.5; //3.0;
  private static final double secondFraction = 1 / 60d / 60d;
  private final static int MAX_HEADING_CHANGE_DERIVATIVE_STEP = 1;
  private static final double DELTA_WEIGHT = 3;
  private final Callsign callsign;
  private final Squawk sqwk;
  private final boolean departure;
  private final Pilot pilot;
  private final AirplaneType airplaneType;
  private double lastHeadingChange = 0;
  private int targetHeading;
  private boolean targetHeadingLeftTurn;
  private double heading;
  private int targetAltitude;
  //private double altitude;
  private int targetSpeed;
  private InertialValue speed;
  private Coordinate coordinate;

  private State state;

  private double lastVerticalSpeed;
  private FlightRecorder flightRecorder = null;
  private boolean airprox;
  private InertialValue altitude;

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
                  int heading, int altitude, int speed, boolean isDeparture,
                  String routeName, SpeechList<IAtcCommand> routeCommandQueue) {

    this.callsign = callsign;
    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneType = airplaneSpecification;

    this.departure = isDeparture;
    this.state = isDeparture ? State.holdingPoint : State.arrivingHigh;

    this.heading = heading;

    this.altitude = new InertialValue(
        altitude,
        this.getType().lowClimbRate / 7d / 60,
        this.getType().highDescendRate / 7d / 60,
        (double) Acc.airport().getAltitude());

    this.speed = new InertialValue(
        speed,
        this.getType().speedIncreaseRate / 4d,
        this.getType().speedDecreaseRate / 6d,
        0d);

    this.targetAltitude = altitude;
    this.targetHeading = heading;
    this.targetSpeed = speed;

    this.pilot = new Pilot(this.new Airplane4Pilot(), routeName, routeCommandQueue);

    // flight recorders on
    this.flightRecorder = FlightRecorder.create(this.callsign, false, true);
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
    return heading;
  }

  public String getHeadingS() {
    return String.format("%1$03d", (int) this.heading);
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

  @Override
  public Callsign getKey() {
    return this.callsign;
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
    return this.callsign.toString();
  }

  public Atc getTunedAtc() {
    return pilot.getTunedAtc();
  }

  public double getTAS() {
    double m = 1 + this.altitude.getValue() / 100000d;
    double ret = this.speed.getValue() * m;
    return ret;
  }

  public double getGS() {
    return getTAS();
  }

  public void setTargetHeading(int targetHeading, boolean useLeftTurn) {
    this.targetHeading = targetHeading;
    this.targetHeadingLeftTurn = useLeftTurn;
  }

  public void setTargetHeading(double targetHeading, boolean useLeftTurn) {
    this.setTargetHeading((int) (Math.round(targetHeading)), useLeftTurn);
  }

  public int getTargetHeading() {
    return targetHeading;
  }

  public void setTargetHeading(double targetHeading) {
    this.setTargetHeading((int) Math.round(targetHeading));
  }

  public void setTargetHeading(int targetHeading) {
    boolean useLeft
        = Headings.getBetterDirectionToTurn(heading, targetHeading) == ChangeHeadingCommand.eDirection.left;
    setTargetHeading(targetHeading, useLeft);
  }

  public int getTargetAltitude() {
    return targetAltitude;
  }

  public void setTargetAltitude(int targetAltitude) {
    this.targetAltitude = targetAltitude;
  }

  public int getTargetSpeed() {
    return targetSpeed;
  }

  public void setTargetSpeed(int targetSpeed) {
    this.targetSpeed = targetSpeed;
  }

  @Override
  public String getName() {
    return this.getCallsign().toString();
  }

  public Navaid getDepartureLastNavaid() {
    if (isDeparture() == false)
      throw new ERuntimeException("This method should not be called on departure aircraft %s.", this.getCallsign().toString());

    String routeName = this.pilot.getRouteName();
    if (routeName.length() > 2 && Character.isDigit(routeName.charAt(routeName.length() - 2)))
      routeName = routeName.substring(0, routeName.length() - 2);
    Navaid ret = Acc.area().getNavaids().tryGet(routeName);
    return ret;
  }

  public boolean isAirprox() {
    return this.airprox;
  }

  public void setAirprox(boolean airprox) {
    this.airprox = airprox;
  }

  public String getRouteNameorFix() {
    return this.pilot.getRouteName();
  }

  public Airplane4Display getInfo() {
    return this.new Airplane4Display();
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc=" private methods ">
  private void drivePlane() {
    pilot.elapseSecond();
  }

  private void processMessages() {
    List<Message> msgs = Acc.messenger().getByTarget(this, true);
    for (Message m : msgs) {
      processMessage(m);
    }
  }

  private void processMessage(Message msg) {
    // if speech from non-tuned ATC, then is ignored
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
        cmds.add(s);
      }
    } else {
      throw new ERuntimeException("Airplane can only deal with messages containing \"IFromAtc\" or \"List<IFromAtc>\".");
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

//  private ValueRequest getSpeedRequest() {
//
//    double delta = targetSpeed - speed.getValue();
//    if (delta == 0) {
//      // no change required
//      return new ValueRequest();
//    }
//
//    double absDelta = delta;
//    double availableStep;
//    if (delta > 0) {
//      // needs to accelerate
//      availableStep = airplaneType.speedIncreaseRate;
//    } else {
//      availableStep = airplaneType.speedDecreaseRate;
//      absDelta = -delta;
//    }
//    if (this.state.isOnGround()) {
//      availableStep = availableStep * GROUND_SPEED_CHANGE_MULTIPLIER;
//    }
//
//    ValueRequest ret = new ValueRequest();
//    if (absDelta < availableStep) {
//      ret.value = absDelta;
//      ret.energy = absDelta / availableStep;
//    } else {
//      ret.value = availableStep;
//      ret.energy = 1;
//    }
//    if (delta < 0)
//      ret.multiply(-1);
//
//    return ret;
//  }

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

    //TODO verify behavior as targetHeading is int and heading is double
    if (targetHeading != heading) {
      adjustHeading();
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
      ret = getRequest(
          this.altitude.getValue(),
          this.targetAltitude,
          airplaneType.getClimbRateForAltitude(this.altitude.getValue()),
          airplaneType.getDescendRateForAltitude(this.altitude.getValue()));
    }

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
    }
  }

  private void adjustHeading() {
    double diff = Headings.getDifference(heading, targetHeading, true);
    //TODO potential problem as "diff" function calculates difference in the shortest arc

    if (diff > lastHeadingChange) {
      lastHeadingChange += MAX_HEADING_CHANGE_DERIVATIVE_STEP;
      if (lastHeadingChange > airplaneType.headingChangeRate) {
        lastHeadingChange = airplaneType.headingChangeRate;
      }
    } else {
      // this is to make slight adjustment, but at least equal to 1 degree
      lastHeadingChange = Math.max(diff - MAX_HEADING_CHANGE_DERIVATIVE_STEP, 1);
    }

    if (targetHeadingLeftTurn)
      this.heading = Headings.add(heading, -lastHeadingChange);
    else
      this.heading = Headings.add(heading, lastHeadingChange);
  }

  private void updateCoordinates() {
    double dist = this.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, heading, dist);

    // add wind if flying
    if (this.getState().is(
        State.holdingPoint,
        State.takeOffRoll,
        State.landed
    ) == false)
      newC = Coordinates.getCoordinate(
          newC,
          Acc.weather().getWindHeading(),
          UnitProvider.ftToNm(Acc.weather().getWindSpeetInKts()));

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
  private static final double INERTIA_WEIGHT = 3;
  private final double maxPositiveInertiaChange;
  private final double maxNegativeInertiaChange;
  private double value;
  private double inertia;
  private Double minimum;

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
    System.out.println("Inert " + this.value + " of " + this.inertia + " - asked " + val);
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
}