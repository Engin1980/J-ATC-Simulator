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

  // get this out someway
  // now it is needed by "draw" cos there dont know
  // who is responsible for plane
  public Atc visuallyResponsibleAtc;

  private final AirplaneType airplaneSpecification;
  
  private final AirplaneInfo info;

  public class AirplaneInfo {

    public String callsign() {
      return Airplane.this.callsign.toString();
    }

    public String callsignCompany() {
      return Airplane.this.callsign.getCompany();
    }

    public String callsignNumber() {
      return Airplane.this.callsign.getNumber();
    }

    public String sqwk() {
      return Airplane.this.sqwk.toString();
    }

    public String altitudeLong() {
      return Acc.toAltS(Airplane.this.altitude, true);
    }

    public String altitudeInFtShort() {
      return Integer.toString(Airplane.this.altitude);
    }

    public String targetAltitudeInFtShort() {
      return Integer.toString(Airplane.this.targetAltitude);
    }

    public String altitudeShort() {
      return Integer.toString(Airplane.this.altitude / 100);
    }

    public String targetAltitudeLong() {
      return Acc.toAltS(Airplane.this.targetAltitude, true);
    }

    public String targetAltitudeShort() {
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

    public String heading() {
      return String.format("%1$3s", Airplane.this.heading);
    }

    public String targetHeading() {
      return String.format("%1$3s", Airplane.this.targetHeading);
    }

    public String speedLong() {
      return Airplane.this.speed + " kt";
    }

    public String speedShort() {
      return Integer.toString(Airplane.this.speed);
    }

    public String targetSpeedLong() {
      return Airplane.this.targetSpeed + " kt";
    }

    public String targetSpeedShort() {
      return Integer.toString(Airplane.this.targetSpeed);
    }

    private Atc pilotAtc;

    public String planeAtcName() {
      return pilotAtc.getName();
    }

    public String planeAtcType() {
      return pilotAtc.getType().toString();
    }

    private Atc responsibleAtc;

    public String responsibleAtcName() {
      return responsibleAtc.getName();
    }

    public String responsibleAtcType() {
      return responsibleAtc.getType().toString();
    }

    public String planeType() {
      return Airplane.this.airplaneSpecification.name;
    }

    public String verticalSpeedLong() {
      return Airplane.this.lastVerticalSpeed + " ft/m";
    }

    public String verticalSpeedShort() {
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
          return this.callsign();
        case 2:
          return this.callsignCompany();
        case 3:
          return this.callsignNumber();
        case 4:
          return this.sqwk();
        case 11:
          return this.heading();
        case 12:
          return this.targetHeading();
        case 21:
          return this.speedLong();
        case 22:
          return this.speedShort();
        case 31:
          return this.targetSpeedLong();
        case 32:
          return this.targetSpeedShort();
        case 33:
          return this.altitudeLong();
        case 34:
          return this.altitudeShort();
        case 35:
          return this.altitudeInFtShort();
        case 36:
          return this.targetAltitudeLong();
        case 37:
          return this.targetAltitudeShort();
        case 38:
          return this.targetAltitudeInFtShort();
        case 41:
          return this.verticalSpeedLong();
        case 42:
          return this.verticalSpeedShort();
        case 43:
          return this.climbDescendChar();
        default:
          return "???";
      }
    }

  }

  public Airplane(Callsign callsign, Coordinate coordinate, Squawk sqwk, AirplaneType airplaneSpecification,
      int heading, int altitude, int speed, boolean isDeparture,
      String routeName, List<Command> routeCommandQueue) {
    
    this.info = this.new AirplaneInfo();
    
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
    int newHeading
        = Headings.turn(heading, airplaneSpecification.headingChangeRate, targetHeadingLeftTurn, targetHeading);
    this.heading = newHeading;
  }

  private void adjustAltitude() {
    int origAlt = altitude;
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

    this.lastVerticalSpeed = (altitude - origAlt) * 60;
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
