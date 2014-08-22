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

  private static Area area;

  private final Callsign callsign;
  private int heading;
  private int altitude;
  private int speed;
  private Coordinate coordinate;
  private char[] sqwk;

  private Atc atc;

  private final AirplaneType airplaneSpecification;

  public static void setArea(Area area) {
    Airplane.area = area;
  }

  public Airplane(Callsign callsign, Coordinate coordinate, char[] sqwk, AirplaneType airplaneSpecification) {
    this.callsign = callsign;
    this.coordinate = coordinate;
    this.setSqwk(sqwk);
    this.airplaneSpecification = airplaneSpecification;

    this.atc = null;
  }

  public Atc getAtc() {
    return atc;
  }

  public void setAtc(Atc atc) {
    this.atc = atc;
  }

  public void setHeading(int heading) {
    while (heading < 0) {
      heading += 360;
    }
    if (heading > 359) {
      heading = heading % 360;
    }
    this.heading = heading;
  }

  public void setAltitude(int altitude) {
    if (altitude < 0) {
      altitude = 0;
    }
    this.altitude = altitude;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
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

  public String getHeadingS(){
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

}
