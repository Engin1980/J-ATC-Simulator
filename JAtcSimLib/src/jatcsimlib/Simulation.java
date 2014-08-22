/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.util.Calendar;

/**
 *
 * @author Marek
 */
public class Simulation {

  private final Calendar now;
  private final Area area;
  private final Airport airport;
  private final AirplaneTypes planeTypes;
  private final AirplaneList planes = new AirplaneList();

  private final EventManager<Simulation, EventListener<Simulation, Simulation>, Simulation> tickEM = new EventManager(this);

  public Area getArea() {
    return area;
  }

  public Airport getAirport() {
    return airport;
  }

  public String toAltitudeString(int altInFt) {
    if (altInFt > getAirport().getTransitionAltitude()) {
      return String.format("FL%03d", altInFt / 1000);
    } else {
      return String.format("%04d", altInFt);
    }
  }

  public Calendar getNow() {
    return now;
  }

  public AirplaneList getPlanes() {
    return planes;
  }

  public Simulation(Area area, Airport airport, AirplaneTypes types, Calendar now) {
    this.area = area;
    Airplane.setArea(area);

    this.airport = airport;
    this.now = now;

    this.planeTypes = types;

    Airplane plane = new Airplane(
        new Callsign("EZY5495"),
        new Coordinate(50.7, 13.5),
        "1400".toCharArray(),
        planeTypes.get(0));

    plane.setAltitude(12000);
    plane.setHeading(90);
    plane.setSpeed(230);

    Atc atc = new UserAtc();
    plane.setAtc(atc);

    planes.add(plane);
  }

  public void elapseSecond() {
    now.add(Calendar.SECOND, 1);

    updatePlanes();

    tickEM.raise(this);
  }

  private void updatePlanes() {
    for (Airplane plane : this.planes) {
      updatePlane(plane);
    }
  }

  private void updatePlane(Airplane plane) {
    updatePlanePosition(plane);
  }

  private final static double SEC_OF_HOUR = 1d / 60 / 60;

  private void updatePlanePosition(Airplane plane) {
    int heading = plane.getHeading();
    int speedKts = plane.getSpeed();

    double distanceInNM = speedKts * SEC_OF_HOUR;
    Coordinate newCoordinate = Coordinates.getCoordinate(plane.getCoordinate(), heading, distanceInNM);
    plane.setCoordinate(newCoordinate);
  }
  
  private static Simulation current;

  public static Simulation getCurrent() {
    return current;
  }

  public static void setCurrent(Simulation current) {
    Simulation.current = current;
  }
  
}
