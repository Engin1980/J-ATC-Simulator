/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.airplanes.AirplaneType;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.CentreAtc;
import jatcsimlib.atcs.TowerAtc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Area;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author Marek
 */
public class Simulation {

  private final ETime now;
  private final Area area;
  private final AirplaneTypes planeTypes;
  private final AirplaneList planes = new AirplaneList();

  private RunwayThreshold activeRunwayThreshold;
  private Weather weather;

  private Messenger messager = new Messenger();
  private final UserAtc appAtc;
  private final TowerAtc twrAtc;
  private final CentreAtc ctrAtc;

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

  public ETime getNow() {
    return now;
  }

  public AirplaneList getPlanes() {
    return planes;
  }

  public Messenger getMessenger() {
    return messager;
  }

  public Simulation(Area area, Airport airport, AirplaneTypes types, Calendar now) {
    this.area = area;
    Airplane.area = area;
    Navaid.area = area;
    this.planeTypes = types;

    this.rebuildParentReferences();
    this.checkRouteCommands();

    this.activeRunwayThreshold
        = airport.getRunways().tryGet("06-24").getThresholdA();
    
    this.twrAtc = new TowerAtc(airport.getIcao());
    this.ctrAtc = new CentreAtc(area.getIcao());
    this.appAtc = new UserAtc(airport.getIcao());
    
    this.now = new ETime(now);
  }

  private boolean isBusy = false;

  public void elapseSecond() {
    if (isBusy) {
      System.out.println("## -- elapse second is busy!");
      return;
    }
    long start = System.currentTimeMillis();
    isBusy = true;
    now.increaseSecond();

    generateNewPlanes();
    updatePlanes();

    long end = System.currentTimeMillis();
    System.out.println("## Sim elapse second: \t" + (end-start));
    
    tickEM.raise(this);
    isBusy = false;
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
    Navaid n;
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
            try{
            n = o.getMainFix();}
            catch (ERuntimeException ex){
              throw new ERuntimeException(
                  String.format(
                      "Airport %s runway %s route %s has no main fix. SID last/STAR first command must be PD FIX (error: %s)",
                      a.getIcao(), t.getName(), o.getName(), o.getRoute()));
            }
          }
        } // for (RunwayThreshold
      } // for (Runway
    } // for (Airport
  }

  private void generateNewPlanes() {
    if (this.now.getSeconds() % 5 != 0) {
      return;
    }
    
    System.out.println("## new plane");

    Airplane plane = generateNewArrivingPlane();

    ctrAtc.registerNewPlane(plane);
    planes.add(plane);
  }

  private Airplane generateNewArrivingPlane() {
    Airplane ret;

    Callsign cs = generateCallsign();
    Route r = getRandomRoute(true);
    Coordinate c = generateArrivalCoordinate(r.getMainFix().getCoordinate(), this.activeRunwayThreshold.getCoordinate());
    char[] sqwk = generateSqwk();
    AirplaneType pt = planeTypes.get(rnd.nextInt(planeTypes.size()));
    int heading = (int) Coordinates.getBearing(c, r.getMainFix().getCoordinate());
    int alt = rnd.nextInt(10000) + 12000;
    int spd = pt.vCruise;

    ret = new Airplane(
        cs, c, sqwk, pt, heading, alt, spd, false);

    // pridat letadlu Route!
    return ret;
  }

  private char[] generateSqwk() {
    int len = 4;
    char[] ret = null;
    while (ret == null) {
      ret = new char[len];
      for (int i = 0; i < len; i++) {
        ret[i] = Integer.toString(rnd.nextInt(8)).charAt(0);
      }
      for (Airplane p : this.planes) {
        if (Arrays.equals(ret, p.getSqwk())) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += rnd.nextDouble() * 50 - 25; // nahodne zatoceni priletoveho radialu
    double dist = rnd.nextDouble() * 10;
    Coordinate ret = null;
    while (ret == null) {

      ret = Coordinates.getCoordinate(navFix, (int) radial, dist);
      for (Airplane p : this.planes) {
        double delta = Coordinates.getDistanceInNM(ret, p.getCoordinate());
        if (delta < 5d) {
          ret = null;
          break;
        }
      }
      dist += rnd.nextDouble() * 10;
    }
    return ret;
  }

  private Callsign generateCallsign() {
    Callsign ret = null;
    while (ret == null) {
      ret = new Callsign("CSA", String.format("%04d", rnd.nextInt(10000)));
      for (Airplane p : this.planes) {
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }

  private static final ERandom rnd = new ERandom();

  private Route getRandomRoute(boolean arrival) {
    Route ret = null;
    while (ret == null) {
      int index = rnd.nextInt(this.activeRunwayThreshold.getRoutes().size());
      if (this.activeRunwayThreshold.getRoutes().get(index).getType().isArrival()) {
        ret = this.activeRunwayThreshold.getRoutes().get(index);
      }
    }
    return ret;
  }

  public Weather getWeather() {
    return weather;
  }

  public UserAtc getAppAtc() {
    return appAtc;
  }

  public TowerAtc getTwrAtc() {
    return twrAtc;
  }

  public CentreAtc getCtrAtc() {
    return ctrAtc;
  }
  
  
}
