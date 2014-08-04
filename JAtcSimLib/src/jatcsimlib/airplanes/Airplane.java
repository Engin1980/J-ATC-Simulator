/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes;

import jatcsimlib.coordinates.Coordinate;

/**
 *
 * @author Marek
 */
public class Airplane {

  private final Callsign callsign;
  private int heading;
  private int altitude;
  private Coordinate coordinate;
  private char[] sqwk;

  private final AirplaneSpecification airplaneSpecification;

  public Airplane(Callsign callsign, Coordinate coordinate, char[] sqwk, AirplaneSpecification airplaneSpecification) {
    this.callsign = callsign;
    this.coordinate = coordinate;
    this.setSqwk(sqwk);
    this.airplaneSpecification = airplaneSpecification;
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

}
