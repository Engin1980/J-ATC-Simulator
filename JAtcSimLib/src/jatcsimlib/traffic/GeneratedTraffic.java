/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import jatcsimlib.Acc;
import static jatcsimlib.Simulation.rnd;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneType;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.AfterAltitudeCommand;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Global;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import jatcsimlib.world.Routes;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class GeneratedTraffic extends Traffic {
  private int maxPlanesInSimulation;
  private final int[] movementsPerHour;
  private int lastHourGeneratedTraffic = -1;
  private final List<Movement> preparedMovements = new LinkedList();

  public GeneratedTraffic(int maxPlanesInSimulation, int[] movementsPerHour) {
    if (maxPlanesInSimulation < 1){
        throw new IllegalArgumentException("Argument \"maxPlanesInSimulation\" must be equal or greather than 1.");
    }
    if (movementsPerHour == null)
      throw new IllegalArgumentException("Argument \"movementsPerHour\" cannot be null.");
    if (movementsPerHour.length != 24)
      throw new IllegalArgumentException("Argument \"movementsPerHour\" must have length equal to 24.");

    this.maxPlanesInSimulation = maxPlanesInSimulation;
    this.movementsPerHour = movementsPerHour;
  }  
  
  @Override
  public Airplane[] getNewAirplanes() {
    generateNewMovementsIfReq();
    
    List<Airplane> ret = new ArrayList();
    while (preparedMovements.size() > 0){
      Movement m = preparedMovements.get(0);
      if (m.getInitTime().isBeforeOrEq(Acc.now())){
        ret.add(generateAirplaneFromMovement(m));
        preparedMovements.remove(m);
      } else
        break; // exit while if no more airplanes are ready
    }
    
    Airplane[] retA = ret.toArray(new Airplane[0]);
    return retA;
  }

  private void generateNewMovementsIfReq() {
    if (lastHourGeneratedTraffic != -1 && Acc.now().getHours() != lastHourGeneratedTraffic)
      return;
    
    int expMovs = movementsPerHour[Acc.now().getHours()];
    for (int i = 0; i < expMovs; i++) {
      Movement m = generateMovement(Acc.now().getHours());
      preparedMovements.add(m);
    }
    Collections.sort(preparedMovements, new MovementSortByETimeComparer());
    lastHourGeneratedTraffic = Acc.now().getHours() + 1;
    if (lastHourGeneratedTraffic > 23) lastHourGeneratedTraffic = 0;
  }

  private Movement generateMovement(int hour) {
    
    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 60), Acc.rnd().nextInt(0, 60));
    boolean isDeparture = (Acc.rnd().nextDouble() <= 0.5);
    Movement ret = new Movement(null, initTime, isDeparture);
    return ret;
    
  }
  
  private Callsign generateCallsign() {
    Callsign ret = null;
    while (ret == null) {
      ret = new Callsign("CSA", String.format("%04d", Acc.rnd().nextInt(10000)));
      for (Airplane p : Acc.planes()) {
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }

    
    /*
        private Airplane generateNewArrivingPlane() {
    Airplane ret;

    Callsign cs = generateCallsign();
    AirplaneType pt = planeTypes.getRandomByTraffic(Acc.airport().getTrafficCategories());

    Route r = tryGetRandomRoute(true, pt);
    if (r == null) {
      r = tryGeneratePointRoute(true);
    }
    Coordinate coord = generateArrivalCoordinate(r.getMainFix().getCoordinate(), Acc.threshold().getCoordinate());
    Squawk sqwk = generateSqwk();

    int heading = (int) Coordinates.getBearing(coord, r.getMainFix().getCoordinate());
    int alt = generateArrivingPlaneAltitude(r);
    int spd = pt.vCruise;

    List<Command> routeCmds = r.getCommandsListClone();
    // added command to descend
    routeCmds.add(0,
        new ChangeAltitudeCommand(
            ChangeAltitudeCommand.eDirection.descend,
            Acc.atcCtr().getOrderedAltitude()
        ));
    // added command to contact CTR
    routeCmds.add(0, new ContactCommand(Atc.eType.ctr));

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        r.getName(), routeCmds);

    return ret;
  }

    */

  private Airplane generateAirplaneFromMovement(Movement m) {
    if (m.isDeparture())
      return generateNewDepartureAirplaneFromMovement(m);
    else
      return generateNewArrivalPlaneFromMovement(m);
  }

  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    if (m.getCallsign() == null)
      cs = generateCallsign();
    else
      cs = m.getCallsign();
    
    AirplaneType pt = Acc.sim().getPlaneTypes().getRandomByTraffic(Acc.airport().getTrafficCategories());

    Route r = tryGetRandomRoute(true, pt);
    if (r == null) {
      r = tryGeneratePointRoute(true);
    }
    Coordinate coord = generateArrivalCoordinate(r.getMainFix().getCoordinate(), Acc.threshold().getCoordinate());
    Squawk sqwk = generateSqwk();

    int heading = (int) Coordinates.getBearing(coord, r.getMainFix().getCoordinate());
    int alt = generateArrivingPlaneAltitude(r);
    int spd = pt.vCruise;

    List<Command> routeCmds = r.getCommandsListClone();
    // added command to descend
    routeCmds.add(0,
        new ChangeAltitudeCommand(
            ChangeAltitudeCommand.eDirection.descend,
            Acc.atcCtr().getOrderedAltitude()
        ));
    // added command to contact CTR
    routeCmds.add(0, new ContactCommand(Atc.eType.ctr));

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        r.getName(), routeCmds);

    return ret;
  }
  
  private int generateArrivingPlaneAltitude(Route r) {
    double thousandsFeetPerMile = 0.30;

    double dist = r.getRouteLength();
    if (dist < 0)
      dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());

    int ret = (int) (dist * thousandsFeetPerMile) + rnd.nextInt(5, 12);
    ret = ret * 1000;
    return ret;
  }
  
  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += rnd.nextDouble() * 50 - 25; // nahodne zatoceni priletoveho radialu
    double dist = rnd.nextDouble() * Global.MAX_ARRIVING_PLANE_DISTANCE; // vzdalenost od prvniho bodu STARu
    Coordinate ret = null;
    while (ret == null) {

      ret = Coordinates.getCoordinate(navFix, (int) radial, dist);
      for (Airplane p : Acc.planes()) {
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
  
  private Squawk generateSqwk() {
    int len = 4;
    char[] tmp;
    Squawk ret = null;
    while (ret == null) {
      tmp = new char[len];
      for (int i = 0; i < len; i++) {
        tmp[i] = Integer.toString(rnd.nextInt(8)).charAt(0);
      }
      ret = Squawk.create(tmp);
      for (Airplane p : Acc.planes()) {
        if (p.getSqwk().equals(ret)) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }
  
  private Route tryGeneratePointRoute(boolean arrival) {
    //1. take points from arriving routes
    List<Navaid> nvs = new LinkedList();

    for (Runway rw : Acc.airport().getRunways()) {
      for (RunwayThreshold rt : rw.getThresholds()) {
        for (Route r : rt.getRoutes()) {
          if (arrival && r.getType() == Route.eType.sid) {
            continue;
          } else if (!arrival && r.getType() != Route.eType.sid) {
            continue;
          }

          Navaid n = r.getMainFix();

          if (nvs.contains(n) == false) {
            nvs.add(n);
          }
        }
      }
    }

    int index = Acc.rnd().nextInt(nvs.size());

    Navaid n = nvs.get(index);

    Route r = Route.createNewByFix(n, arrival);

    return r;
  }
  
  private Route tryGetRandomRoute(boolean arrival, AirplaneType planeType) {

    Iterable<Route> rts = Acc.threshold().getRoutes();
    List<Route> avails = Routes.getByFilter(rts, arrival, planeType.category);

    if (avails.isEmpty()) {
      return null; // if no route, return null
    }
    int index = rnd.nextInt(avails.size());

    Route ret = avails.get(index);

    return ret;
  }

  private Airplane generateNewDepartureAirplaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    if (m.getCallsign() == null)
      cs = generateCallsign();
    else
      cs = m.getCallsign();
    AirplaneType pt = Acc.sim().getPlaneTypes().getRandomByTraffic(Acc.airport().getTrafficCategories());

    Route r = tryGetRandomRoute(false, pt);
    Coordinate coord = Acc.threshold().getCoordinate();
    Squawk sqwk = generateSqwk();

    int heading = (int) Acc.threshold().getCourse();
    int alt = Acc.threshold().getParent().getParent().getAltitude();
    int spd = 0;

    List<Command> routeCmds = r.getCommandsListClone();

    int indx = 0;
    // added command to contact after departure
    routeCmds.add(indx++, new ContactCommand(Atc.eType.twr));

    routeCmds.add(indx++, new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.climb, Acc.threshold().getInitialDepartureAltitude()));

    // -- po vysce+300 ma kontaktovat APP
    routeCmds.add(indx++,
        new AfterAltitudeCommand(Acc.threshold().getParent().getParent().getAltitude() + Acc.rnd().nextInt(150, 450)));
    routeCmds.add(indx++, new ContactCommand(Atc.eType.app));

    // -- po vysce + 3000 rychlost na odlet
//    routeCmds.add(indx++,
//        new AfterAltitudeCommand(Acc.threshold().getParent().getParent().getAltitude() + 3000));
//    routeCmds.add(indx++, new ChangeSpeedCommand(ChangeSpeedCommand.eDirection.increase, 250));

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, true,
        r.getName(), routeCmds);

    return ret;
  }
  
}

// <editor-fold defaultstate="collapsed" desc=" MovementSortByETimeComparer ">

class MovementSortByETimeComparer implements Comparator<Movement>{

  @Override
  public int compare(Movement o1, Movement o2) {
    return o1.getInitTime().compareTo(o2.getInitTime());
  }
  
}

// </editor-fold>