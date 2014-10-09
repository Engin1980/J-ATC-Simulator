/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.pilots.Pilot;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.ChangeHeadingCommand;
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

  private int lastVerticalSpeed;

  private final AirplaneType airplaneType;

  private final AirplaneInfo info;

  public class AirplaneInfo {

    public Coordinate coordinate() {
      return Airplane.this.coordinate;
    }

    public Callsign callsign() {
      return Airplane.this.callsign;
    }

    public String callsignS() {
      return Airplane.this.callsign.toString();
    }

    public String callsignCompany() {
      return Airplane.this.callsign.getCompany();
    }

    public String callsignNumber() {
      return Airplane.this.callsign.getNumber();
    }

    public String sqwkS() {
      return Airplane.this.sqwk.toString();
    }

    public Atc tunedAtc() {
      return pilot.getTunedAtc();
    }

    public Atc responsibleAtc() {
      return Acc.prm().getResponsibleAtc(Airplane.this);
    }

    public int altitude() {
      return Airplane.this.altitude;
    }

    public String altitudeSLong() {
      return Acc.toAltS(Airplane.this.altitude, true);
    }

    public String altitudeSInFtShort() {
      return Integer.toString(Airplane.this.altitude);
    }

    public String targetAltitudeSInFtShort() {
      return Integer.toString(Airplane.this.targetAltitude);
    }

    public String altitudeSShort() {
      return Integer.toString(Airplane.this.altitude / 100);
    }
    
    public String altitudeSFixed(){
      return String.format("%1$03d", Airplane.this.altitude / 100);
    }
    
    public String targetAltitudeSFixed(){
      return String.format("%1$03d", Airplane.this.targetAltitude / 100);
    }

    public String targetAltitudeSLong() {
      return Acc.toAltS(Airplane.this.targetAltitude, true);
    }

    public String targetAltitudeSShort() {
      return Integer.toString(Airplane.this.targetAltitude / 100);
    }

    public String climbDescendChar() {
      if (Airplane.this.targetAltitude > Airplane.this.altitude) {
        return "↑"; //"▲";
      } else if (Airplane.this.targetAltitude < Airplane.this.altitude) {
        return "↓"; // "▼";
      } else {
        return "=";
      }
    }
    
    public String departureArrivalChar(){
      if (isDeparture())
        return "▲"; //"↑"; //
      else
        return "▼"; // "↓";
    }

    public int heading() {
      return Airplane.this.heading;
    }

    public String headingSLong() {
      return String.format("%1$03d", Airplane.this.heading);
    }
    
    public String headingSShort() {
      return Integer.toString(Airplane.this.heading);
    }

    public String targetHeadingSLong() {
      return String.format("%1$03d", Airplane.this.targetHeading);
    }
    
    public String targetHeadingSShort() {
      return Integer.toString(Airplane.this.targetHeading);
    }

    public int speed() {
      return Airplane.this.speed;
    }

    public String speedSLong() {
      return Airplane.this.speed + " kt";
    }

    public String speedSShort() {
      return Integer.toString(Airplane.this.speed);
    }

    public String speedSShortAligned() {
      return String.format("%1# 3d", Airplane.this.speed);
    }

    public int targetSpeed() {
      return Airplane.this.targetSpeed;
    }

    public String targetSpeedSLong() {
      return Airplane.this.targetSpeed + " kt";
    }

    public String targetSpeedSShort() {
      return Integer.toString(Airplane.this.targetSpeed);
    }

    public String planeAtcName() {
      return tunedAtc().getName();
    }

    public Atc.eType planeAtcType() {
      return tunedAtc().getType();
    }

    public String responsibleAtcName() {
      return responsibleAtc().getName();
    }

    public Atc.eType responsibleAtcType() {
      return responsibleAtc().getType();
    }

    public String planeType() {
      return Airplane.this.airplaneType.name;
    }

    public int verticalSpeed() {
      return Airplane.this.lastVerticalSpeed;
    }

    public String verticalSpeedSLong() {
      return Airplane.this.lastVerticalSpeed + " ft/m";
    }

    public String verticalSpeedSShort() {
      return Integer.toString(Airplane.this.lastVerticalSpeed);
    }

    public String format(String pattern) {
      StringBuilder sb = new StringBuilder(pattern);
      int[] p = new int[2];

      while (true) {
        updatePair(sb, p);
        if (p[0] < 0) {
          break;
        }

        String tmp = sb.substring(p[0] + 1, p[1]);
        int index = Integer.parseInt(tmp);
        sb.replace(p[0], p[1] + 1, getFormatValueByIndex(index));
      }

      return sb.toString();
    }

    public String typeName() {
      return Airplane.this.airplaneType.name;
    }

    public String typeCategory() {
      return Character.toString(Airplane.this.airplaneType.category);
    }

    private void updatePair(StringBuilder ret, int[] p) {
      int start = ret.indexOf("{");
      if (start < 0) {
        p[0] = -1;
        return;
      }
      p[0] = start;
      int end = ret.indexOf("}", start);
      p[1] = end;
    }

    private String getFormatValueByIndex(int index) {
      switch (index) {
        case 1:
          return this.callsignS();
        case 2:
          return this.callsignCompany();
        case 3:
          return this.callsignNumber();
        case 4:
          return this.typeName();
        case 5:
          return this.typeCategory();
        case 8:
          return this.sqwkS();
        case 11:
          return this.headingSLong();
        case 12:
          return this.headingSShort();
        case 15:
          return this.targetHeadingSLong();
        case 16:
          return this.targetHeadingSShort();
        case 21:
          return this.speedSLong();
        case 22:
          return this.speedSShort();
        case 23:
          return this.speedSShortAligned();
        case 31:
          return this.targetSpeedSLong();
        case 32:
          return this.targetSpeedSShort();
        case 33:
          return this.altitudeSLong();
        case 34:
          return this.altitudeSShort();
        case 35:
          return this.altitudeSInFtShort();
        case 36:
          return this.targetAltitudeSLong();
        case 37:
          return this.targetAltitudeSShort();
        case 38:
          return this.targetAltitudeSInFtShort();
        case 41:
          return this.verticalSpeedSLong();
        case 42:
          return this.verticalSpeedSShort();
        case 43:
          return this.climbDescendChar();
        default:
          return "???";
      }
    }

    private boolean airprox = false;

    public boolean isAirprox() {
      return airprox;
    }

    void setAirprox(boolean airprox) {
      this.airprox = airprox;
    }

    public boolean isDeparture() {
      return Airplane.this.departure;
    }

    public String routeNameOrFix() {
      return Airplane.this.pilot.getRouteName();
    }

  }

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
      int heading, int altitude, int speed, boolean isDeparture,
      String routeName, List<Command> routeCommandQueue) {

    this.info = this.new AirplaneInfo();

    this.callsign = callsign;
    this.coordinate = coordinate;
    this.sqwk = sqwk;
    this.airplaneType = airplaneSpecification;

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
    heading = Headings.to(heading);

    if (speed < 0) {
      speed = 0;
    }

    if (altitude < 0) {
      altitude = 0;
    }
  }

  public AirplaneInfo getInfo() {
    return info;
  }

  public boolean isDeparture() {
    return departure;
  }

  public boolean isArrival() {
    return !departure;
  }

  public int getVerticalSpeed() {
    return lastVerticalSpeed;
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

  public boolean isFlying() {
    return altitude > Acc.airport().getAltitude();
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public Squawk getSqwk() {
    return sqwk;
  }

  public AirplaneType getAirplaneType() {
    return airplaneType;
  }

  public int getSpeed() {
    return speed;
  }

  @Override
  public Callsign getKey() {
    return this.callsign;
  }

  public void elapseSecond() {

    processMessages();
    drivePlane();
    updateSHABySecond();
    updateCoordinates();
  }

  private void drivePlane() {
    pilot.elapseSecond();
  }

  private void processMessages() {
    List<Message> msgs = Acc.messenger().getMy(this, true);

    for (Message m : msgs) {
      processMessage(m);
    }
  }

  private void processMessage(Message msg) {
    List<Command> cmds;
    Object s = msg.content;
    if (s instanceof Command) {
      cmds = new ArrayList<>(1);
      cmds.add((Command) s);
    } else if (s instanceof List) {
      cmds = (List<Command>) s;
    } else {
      throw new ERuntimeException("Airplane can only deal with messages containing \"Command\" or \"List<Command>\".");
    }

    processCommands(cmds);
  }

  private void processCommands(List<Command> cmds) {
    this.pilot.addNewCommands(cmds);
  }

  private void updateSHABySecond() {
    if (targetSpeed != speed) {
      adjustSpeed();
    }
    if (targetHeading != heading) {
      adjustHeading();
    }
    if (targetAltitude != altitude) {
      adjustAltitude();
    }

    updateCoordinates();
  }

  private final static double GROUND_MULTIPLIER = 3.0;

  private void adjustSpeed() {
    //boolean onGround = altitude == Acc.airport().getAltitude();
    // this is faster:
    boolean onGround = speed < airplaneType.vMinApp && speed > 20;
    // in flight
    if (targetSpeed > speed) {
      int step = airplaneType.speedIncreaseRate;
      if (onGround) {
        step = (int) Math.ceil(step * GROUND_MULTIPLIER);
      }
      speed += step;
      if (targetSpeed < speed) {
        speed = targetSpeed;
      }
    } else if (targetSpeed < speed) {
      int step = airplaneType.speedDecreaseRate;
      if (onGround) {
        step = (int) Math.ceil(step * GROUND_MULTIPLIER);
      }
      speed -= step;
      if (targetSpeed > speed) {
        speed = targetSpeed;
      }
    } // else if (targetSpeed < speed)
  }

  private void adjustHeading() {
    int newHeading
        = Headings.turn(heading, airplaneType.headingChangeRate, targetHeadingLeftTurn, targetHeading);
    this.heading = newHeading;
  }

  private void adjustAltitude() {
    if (speed < airplaneType.vR) {
      return;
    }

    int origAlt = altitude;
    if (targetAltitude > altitude) {
      int step = (int) (airplaneType.getClimbRateForAltitude(this.altitude));
      altitude += step;
      if (targetAltitude < altitude) {
        altitude = targetAltitude;
      }
    } else if (targetAltitude < altitude) {
      int step = (int) (airplaneType.getDescendRateForAltitude(this.altitude));
      altitude -= step;
      if (targetAltitude > altitude) {
        altitude = targetAltitude;
      }
    }

    this.lastVerticalSpeed = (altitude - origAlt) * 60;
  }

  public Atc getTunedAtc() {
    return pilot.getTunedAtc();
  }

  public int getTAS() {
    double m = 1 + this.altitude / 100000d;
    int ret = (int) (this.speed * m);
    return ret;
  }

  public int getGS() {
    return getTAS();
  }

  private static final double secondFraction = 1 / 60d / 60d;

  private void updateCoordinates() {
    double dist = this.getGS() * secondFraction;
    Coordinate newC
        = Coordinates.getCoordinate(coordinate, heading, dist);
    this.coordinate = newC;
  }

  public void setTargetHeading(int targetHeading) {
    boolean useLeft
        = Headings.getBetterDirectionToTurn(heading, targetHeading) == ChangeHeadingCommand.eDirection.left;
    setTargetHeading(targetHeading, useLeft);
  }

  public void setTargetHeading(int targetHeading, boolean useLeftTurn) {
    this.targetHeading = targetHeading;
    this.targetHeadingLeftTurn = useLeftTurn;
  }

  public void setTargetSpeed(int targetSpeed) {
    this.targetSpeed = targetSpeed;
  }

  public void setTargetAltitude(int targetAltitude) {
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
