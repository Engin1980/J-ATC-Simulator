/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.atcs.Atc;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.world.Area;

/**
 *
 * @author Marek
 */
public class Airplane implements KeyItem<Callsign> {

  private enum eTurnDirection {

    left,
    right
  }

  private static Area area;

  private final Callsign callsign;
  private int targetHeading;
  private eTurnDirection targetHeadingTurnDirection;
  private int heading;
  private int targetAltitude;
  private int altitude;
  private int targetSpeed;
  private int speed;
  private Coordinate coordinate;
  private char[] sqwk;

  private Atc atc;

  private final AirplaneType airplaneSpecification;

  public static void setArea(Area area) {
    Airplane.area = area;
  }

  public Airplane(Callsign callsign, Coordinate coordinate, char[] sqwk, AirplaneType airplaneSpecification,
      int heading, int altitude, int speed) {
    this.callsign = callsign;
    this.coordinate = coordinate;
    this.setSqwk(sqwk);
    this.airplaneSpecification = airplaneSpecification;

    this.atc = null;

    this.heading = heading;
    this.altitude = altitude;
    this.speed = speed;
    this.ensureSanity();
    this.targetAltitude = this.altitude;
    this.targetHeading = this.heading;
    this.targetSpeed = this.speed;
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

  public Atc getAtc() {
    return atc;
  }

  public void setAtc(Atc atc) {
    this.atc = atc;
  }

  public final void setSqwk(char[] sqwk) {
    if (sqwk.length != 4) {
      throw new IllegalArgumentException("Sqwk length must be 4");
    }
    for (int i = 0; i < sqwk.length; i++) {
      char c = sqwk[i];
      if (c < '0' || c > '7') {
        throw new IllegalArgumentException("Sqwk length must character 0-7.");
      }
    }
    this.sqwk = sqwk;
  }

  public void setCoordinate(Coordinate coordinate) {
    this.coordinate = coordinate;
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

  public String getAltitudeS(int transitionLevel) {
    if (altitude > transitionLevel) {
      return "FL" + (altitude / 100);
    } else {
      return Integer.toString(altitude);
    }
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public char[] getSqwk() {
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

  private void rollSpeed(int targetSpeed) {
    this.targetSpeed = targetSpeed;
  }

  private void rollAltitude(int targetAltitude) {
    this.targetAltitude = targetAltitude;
  }

  private void rollHeading(int targetHeading, eTurnDirection turn) {
    this.targetHeadingTurnDirection = turn;
    this.targetHeading = heading;
  }

  private void processSimulationSecond() {
    if (speed != targetSpeed) {
      adjustSpeed();
    }
    adjustHeading();
    adjustAltitude();

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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  private void adjustAltitude() {
    if (targetAltitude > altitude) {
      int step = airplaneSpecification.speedIncreaseRate;
      speed += step;
      if (targetAltitude < altitude) {
        speed = targetAltitude;
      }
    } else if (targetAltitude < altitude) {
      int step = airplaneSpecification.speedDecreaseRate;
      speed -= step;
      if (targetAltitude > speed) {
        speed = targetAltitude;
      }
    }
  }

  private void updateCoordinates() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
