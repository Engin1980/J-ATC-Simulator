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
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Area;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import java.util.Calendar;

/**
 *
 * @author Marek
 */
public class Simulation {

  private final Calendar now;
  private final Area area;
  private final AirplaneTypes planeTypes;
  private final AirplaneList planes = new AirplaneList();

  private RunwayThreshold activeRunwayThreshold;
  private Weather weather;

  private final EventManager<Simulation, EventListener<Simulation, Simulation>, Simulation> tickEM = new EventManager(this);

  public Area getArea() {
    return area;
  }

  public RunwayThreshold getActiveRunwayThreshold() {
    return activeRunwayThreshold;
  }

  public Airport getActiveAirport() {
    return activeRunwayThreshold.getParent().getParent();
  }

  public String toAltitudeString(int altInFt) {
    if (altInFt > getActiveAirport().getTransitionAltitude()) {
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
    Airplane.area = area;
    Navaid.area = area;
    this.planeTypes = types;

    this.rebuildParentReferences();
    this.checkRouteCommands();

    this.now = now;

    this.activeRunwayThreshold
        = airport.getRunways().get("06-24").getThresholdA();
  }

  public void elapseSecond() {
    now.add(Calendar.SECOND, 1);

    generateNewPlanes();
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

  private void rebuildParentReferences() {
    for (Airport a : this.area.getAirports()) {
      a.setParent(this.area);

      for (Runway r : a.getRunways()) {
        r.setParent(a);

        for (RunwayThreshold t : r.getThresholds()) {
          t.setParent(r);

          for (Route o : t.getRoutes()) {
            o.setParent(t);
          }
          for (Approach p : t.getApproaches()) {
            p.setParent(t);
          }
        }
      }
    }
  }

  private void checkRouteCommands() {
    Command[] cmds;
    for (Airport a : this.area.getAirports()) {
      for (Runway r : a.getRunways()) {
        for (RunwayThreshold t : r.getThresholds()) {
          for (Approach p : t.getApproaches()) {
            try {
              cmds = CommandFormat.parseMulti(p.getGaRoute());
            } catch (Exception ex) {
              throw new ERuntimeException(
                  String.format("Airport %s runway %s approach %s has invalid go-around route commands: %s (error: %s)",
                      a.getIcao(), t.getName(), p.getType(), p.getGaRoute(), ex.getMessage()));
            }
          } // for (Approach

          for (Route o : t.getRoutes()) {
            try {
              cmds = CommandFormat.parseMulti(o.getRoute());
            } catch (Exception ex) {
              throw new ERuntimeException(
                  String.format("Airport %s runway %s route %s has invalid commands: %s (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute(), ex.getMessage()));
            }
          }
        } // for (RunwayThreshold
      } // for (Runway
    } // for (Airport
  }

  private void generateNewPlanes() {
    if (planes.size() != 0) {
      return;
    }

    Airplane plane = new Airplane(
        new Callsign("EZY5495"),
        new Coordinate(50.7, 13.5),
        "1400".toCharArray(),
        planeTypes.get(0),
        90, 12000, 230);

    Atc atc = new UserAtc();
    plane.setAtc(atc);

    planes.add(plane);
  }
}
