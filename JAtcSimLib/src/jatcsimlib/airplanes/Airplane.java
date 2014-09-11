/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.pilots.Pilot;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.Command;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.Headings;
import jatcsimlib.global.KeyItem;
import jatcsimlib.messaging.Message;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Airplane implements KeyItem<Callsign> {

  private final Callsign callsign;
  
  private int targetHeading;
  private boolean targetHeadingLeftTurn;
  private int heading;
  
  private int targetAltitude;
  private int altitude;
  
  private int targetSpeed;
  private int speed;
  
  private Coordinate coordinate;
  
  private final Squawk sqwk;
  
  private final boolean departure;

  private final Pilot pilot;
  
  // get this out someway
  // now it is needed by "draw" cos there dont know
  // who is responsible for plane
  public Atc visuallyResponsibleAtc;

  private final AirplaneType airplaneSpecification;

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
      int heading, int altitude, int speed, boolean isDeparture,
      String routeName, List<Command> routeCommandQueue) {
    this.callsign = callsign;
    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneSpecification = airplaneSpecification;

    this.departure = isDeparture;

    this.heading = heading;
    this.altitude = altitude;
    this.speed = speed;
    this.ensureSanity();
    this.targetAltitude = this.altitude;
    this.targetHeading = this.heading;
    this.targetSpeed = this.speed;
    
    this.pilot = new Pilot(this, routeName, routeCommandQueue);
  }

  private void ensureSanity() {
    while (heading < 0) {
      heading += 360;
    }
    if (heading > 359) {
      heading %= 360;
    }

    if (speed < 0) {
      speed = 0;
    }

    if (altitude < 0) {
      altitude = 0;
    }
  }

  public boolean isDeparture(){
    return departure;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getHeading() {
    return heading;
  }

  public String getHeadingS() {
    return String.format("%1$03d", this.heading);
  }

  public int getAltitude() {
    return altitude;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Squawk getSqwk() {
    return sqwk;
  }

  public AirplaneType getAirplaneSpecification() {
    return airplaneSpecification;
  }

  public int getSpeed() {
    return speed;
  }

  @Override
  public Callsign getKey() {
    return this.callsign;
  }

  public void elapseSecond(){
    
    processMessages();
    drivePlane();
    updateSHABySecond();
    updateCoordinates();
  }
  
  private void drivePlane(){
    pilot.drivePlane();
  }
  
  private void processMessages(){
    List<Message> msgs = Acc.messenger().getMy(this, true);
    
    for (Message m : msgs){
      processMessage(m);
    }
  } 
  
  private void processMessage(Message msg){
    List<Command> cmds;
    Object s = msg.content;
    if (s instanceof Command){
      cmds = new ArrayList<>(1);
      cmds.add((Command) s);
    } else if (s instanceof List) {
      cmds = (List<Command>) s;
    } else
      throw new ERuntimeException("Airplane can only deal with messages containing \"Command\" or \"List<Command>\".");
    
    processCommands(cmds);
  }
  
  private void processCommands(List<Command> cmds){
   this.pilot.addNewCommands(cmds);
  }
  
  private void updateSHABySecond() {
    if (targetSpeed != speed)  adjustSpeed();
    if (targetHeading != heading) adjustHeading();
    if (targetAltitude != altitude) adjustAltitude();

    updateCoordinates();
  }

  private void adjustSpeed() {
    if (targetSpeed > speed) {
      int step = airplaneSpecification.speedIncreaseRate;
      speed += step;
      if (targetSpeed < speed) {
        speed = targetSpeed;
      }
    } else if (targetSpeed < speed) {
      int step = airplaneSpecification.speedDecreaseRate;
      speed -= step;
      if (targetSpeed > speed) {
        speed = targetSpeed;
      }
    }
  }

  private void adjustHeading() {
    int newHeading = 
        Headings.turn(heading, airplaneSpecification.headingChangeRate, targetHeadingLeftTurn, targetHeading);
    this.heading = newHeading;
  }

  private void adjustAltitude() {
    if (targetAltitude > altitude) {
      int step = (int) (airplaneSpecification.getClimbRateForAltitude(this.altitude));
      altitude += step;
      if (targetAltitude < altitude) {
        altitude = targetAltitude;
      }
    } else if (targetAltitude < altitude) {
      int step = (int) (airplaneSpecification.getDescendRateForAltitude(this.altitude));
      altitude -= step;
      if (targetAltitude > altitude) {
        altitude = targetAltitude;
      }
    }
  }

  public int getTAS(){
    double m = 1 + this.altitude / 100000d;
    int ret = (int) (this.speed * m);
    return ret;
  }
  
  public int getGS(){
    return getTAS();
  }
  
  private static final double secondFraction = 1/60d/60d;
  private void updateCoordinates() {
    double dist = this.getGS() * secondFraction;
    Coordinate newC = 
        Coordinates.getCoordinate(coordinate, heading, dist);
    this.coordinate = newC;
  }
  
  public void setTargetHeading(int targetHeading, boolean useLeftTurn){
    this.targetHeading = targetHeading;
    this.targetHeadingLeftTurn = useLeftTurn;
  }
  public void setTargetSpeed(int targetSpeed){
    this.targetSpeed = targetSpeed;
  }
  public void setTargetAltitude(int targetAltitude){
    this.targetAltitude = targetAltitude;
  }

  public int getTargetHeading() {
    return targetHeading;
  }

  public int getTargetAltitude() {
    return targetAltitude;
  }

  public int getTargetSpeed() {
    return targetSpeed;
  }

  @Override
  public String toString() {
    return this.callsign.toString();
  }
  
  
}
