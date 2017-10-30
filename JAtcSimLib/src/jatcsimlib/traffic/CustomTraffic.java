/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import com.sun.org.apache.bcel.internal.generic.RET;
import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.airplanes.AirplaneType;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.*;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Global;
import jatcsimlib.global.KeyList;
import jatcsimlib.world.*;

import java.util.*;

import static jatcsimlib.Simulation.rnd;

/**
 * @author Marek Vajgl
 */
public class CustomTraffic extends Traffic {

  private static final String[] COMPANIES = new String[]{"CSA", "EZY", "BAW", "RYR"};
  private static final String[] PLANE_COUNTRY_CODES = new String[]{"OK", "OM"};
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.7;

  /**
   * Max number of planes in simulation
   */
  private final int maxPlanesInSimulation;

  /**
   * Probability that generated plane is departure (arrival otherwise), range 0.0-1.0 .
   */
  private final double probabilityOfDeparture;

  /**
   * Probability that generated plane is IFR (VFR otherwise), range 0.0-1.0.
   */
  private final double probabilityOfIfr;

  /**
   * Specifies number of movements for each hour. This is 24 item array, where for each hour number movements is
   * defined.
   */
  private final int[] movementsPerHour = new int[24];

  /**
   * Specifies aggregated(!) probability thresholds of each category. 0 = A, 1 = B, 2 = C, 3 = D, this[4] should be
   * allways 1. Generated VFR is allways A.
   */
  private final double[] probabilityOfCategory = new double[4];

  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;

  /**
   * Specifies if extended callsigns containing characters at the end can be used.
   */
  private final boolean useExtendedCallsigns;

  /**
   * How many minutes takes it approximately to fly whole approach.
   */
  private final int arrivalRouteTimeInMinutes = 20;


  private final List<Movement> preparedMovements = new LinkedList();
  private int nextHourToGenerateTraffic = -1;

  public CustomTraffic(int movementsPerHour, double probabilityOfDeparture, int maxPlanesInSimulation, double probabilityOfIfr,
                       int trafficCustomWeightTypeA, int trafficCustomWeightTypeB, int trafficCustomWeightTypeC, int trafficCustomWeightTypeD,
                       boolean useExtendedCallsigns) {

    if (movementsPerHour < 0) {
      throw new IllegalArgumentException("Argument \"movementsPerHour\" must be equal or greater than 0.");
    }

    if (maxPlanesInSimulation < 1) {
      throw new IllegalArgumentException("Argument \"maxPlanesInSimulation\" must be equal or greater than 1.");
    }

    if (eng.eSystem.Number.isBetweenOrEqual(0, probabilityOfDeparture, 1) == false) {
      throw new IllegalArgumentException("\"probabilityOfDeparture\" must be between 0 and 1.");
    }

    if (eng.eSystem.Number.isBetweenOrEqual(0, probabilityOfIfr, 1) == false) {
      throw new IllegalArgumentException("\"probabilityOfIfr\" must be between 0 and 1.");
    }

    for (int i = 0; i < this.movementsPerHour.length; i++) {
      this.movementsPerHour[i] = movementsPerHour;
    }
    this.maxPlanesInSimulation = maxPlanesInSimulation;
    this.probabilityOfDeparture = probabilityOfDeparture;
    this.probabilityOfIfr = probabilityOfIfr;

    // category probabilities init
    {
      double sum = trafficCustomWeightTypeA + trafficCustomWeightTypeB + trafficCustomWeightTypeC + trafficCustomWeightTypeD;
      probabilityOfCategory[0] = trafficCustomWeightTypeA / sum;
      probabilityOfCategory[1] = probabilityOfCategory[0] + trafficCustomWeightTypeB / sum;
      probabilityOfCategory[2] = probabilityOfCategory[1] + trafficCustomWeightTypeC / sum;
      probabilityOfCategory[3] = 1;
    }

    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  @Override
  public Airplane[] getNewAirplanes() {

    List<Airplane> ret = new ArrayList();
    while (preparedMovements.size() > 0) {

      Movement m = preparedMovements.get(0);
      if (m.getInitTime().isBeforeOrEq(Acc.now())) {

        // check for too many planes. If true, movement is dropped
        if (Acc.planes().size() + ret.size() < maxPlanesInSimulation) {
          Airplane a = generateAirplaneFromMovement(m);
          ret.add(a);
        }

        preparedMovements.remove(m);
      } else {
        break; // exit while if no more airplanes are ready
      }
    }

    Airplane[] retA = ret.toArray(new Airplane[0]);
    return retA;
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (nextHourToGenerateTraffic != -1 && Acc.now().getHours() != nextHourToGenerateTraffic) {
      return;
    }

    int currentHour = Acc.now().getHours();
    int expMovs = movementsPerHour[currentHour];
    for (int i = 0; i < expMovs; i++) {
      Movement m = generateMovement(currentHour);
      preparedMovements.add(m);
    }
    Collections.sort(preparedMovements, new Movement.SortByETimeComparer());
    nextHourToGenerateTraffic = currentHour + 1;
    if (nextHourToGenerateTraffic > 23) {
      nextHourToGenerateTraffic = 0;
    }
  }

  @Override
  public Movement[] getScheduledMovements() {
    Movement [] ret =
        preparedMovements.toArray(new Movement[0]);
    return ret;
  }

  private Movement generateMovement(int hour) {

    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 60), Acc.rnd().nextInt(0, 60));
    boolean isDeparture = (Acc.rnd().nextDouble() <= this.probabilityOfDeparture);
    boolean isIfr = Acc.rnd().nextDouble() <= this.probabilityOfIfr;
    Callsign cls = generateCallsign(isIfr);

    int delayInMinutes = generateDelayMinutes();
    ETime scheduledTime = isDeparture ? initTime.clone() : initTime.addMinutes(arrivalRouteTimeInMinutes);

    Movement ret = new Movement(cls, initTime, delayInMinutes, isDeparture, isIfr);
    return ret;

  }

  private int generateDelayMinutes() {
    int ret = 0;
    while (Acc.rnd().nextDouble() < delayProbability) {
      int del = Acc.rnd().nextInt(maxDelayInMinutesPerStep);
      ret += del;
    }
    return ret;
  }

  private Callsign generateCallsign(boolean isIfr) {
    Callsign ret = null;

    while (ret == null) {

      ret = buildRandomCallsign(isIfr);
      for (Airplane p : Acc.planes()) { // check not existing in current planes
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
      for (Movement m : this.preparedMovements) { // check not existing in future planes
        if (m.getCallsign().equals(ret)) {
          ret = null;
          break;
        }
      }

    }

    return ret;
  }

  private Callsign buildRandomCallsign(boolean isIfr) {
    String prefix;
    StringBuilder sb = new StringBuilder();
    if (isIfr) {
      prefix = eng.eSystem.Arrays.getRandom(COMPANIES);
      if (this.useExtendedCallsigns && Acc.rnd().nextDouble() < EXTENDED_CALLSIGN_PROBABILITY) {
        sb.append(getRandomCallsignChar('0', '9'));
        sb.append(getRandomCallsignChar('A', 'Z'));
        sb.append(getRandomCallsignChar('A', 'Z'));
      } else {
        sb.append(getRandomCallsignChar('0', '9'));
        sb.append(getRandomCallsignChar('0', '9'));
        sb.append(getRandomCallsignChar('0', '9'));
        sb.append(getRandomCallsignChar('0', '9'));
      }
    } else {
      prefix = eng.eSystem.Arrays.getRandom(PLANE_COUNTRY_CODES);
      sb.append(getRandomCallsignChar('A', 'Z'));
      sb.append(getRandomCallsignChar('A', 'Z'));
      sb.append(getRandomCallsignChar('A', 'Z'));
    }
    Callsign ret = new Callsign(prefix, sb.toString());
    return ret;
  }

  private char getRandomCallsignChar(char from, char to) {
    int val = Acc.rnd().nextInt(from, to + 1);
    char ret = (char) val;
    return ret;
  }

  private Airplane generateAirplaneFromMovement(Movement m) {
    if (m.isDeparture()) {
      return generateNewDepartureAirplaneFromMovement(m);
    } else {
      return generateNewArrivalPlaneFromMovement(m);
    }
  }

  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();

    AirplaneType pt = Acc.sim().getPlaneTypes().getRandomByTraffic(Acc.airport().getTrafficCategories(), m.isIfr());

    Route r;
    Coordinate coord;
    int heading;
    int alt;
    int spd;
    List<Command> routeCmds;
    String routeName;

    if (m.isIfr()) {
      // I F R   flight
      r = tryGetRandomIfrRoute(true, pt);
      if (r == null) {
        r = tryGeneratePointRoute(true);
        //TODO this function should not be called "tryGenerate..." as its expected always to return something
      }
      coord = generateArrivalCoordinate(r.getMainFix().getCoordinate(), Acc.threshold().getCoordinate());
      heading = (int) Coordinates.getBearing(coord, r.getMainFix().getCoordinate());
      alt = generateArrivingPlaneAltitude(r);

      routeCmds = r.getCommandsListClone();
      // added command to descend
      routeCmds.add(0,
          new ChangeAltitudeCommand(
              ChangeAltitudeCommand.eDirection.descend,
              Acc.atcCtr().getOrderedAltitude()
          ));
      // added command to contact CTR
      routeCmds.add(0, new ContactCommand(Atc.eType.ctr));

      routeName = r.getName();
    } else {
      // V F R    flight
      VfrPoint entryPoint = getRandomVfrEntryPoint(Acc.airport().getVfrPoints());
      coord = generateArrivalCoordinate(entryPoint.getCoordinate(), Acc.threshold().getCoordinate());
      heading = (int) Coordinates.getBearing(coord, entryPoint.getCoordinate());
      alt = Acc.airport().getVfrAltitude();

      routeCmds = new LinkedList<>();
      routeCmds.add(0,
          new ProceedDirectCommand(Acc.airport().getMainAirportNavaid()));
      routeCmds.add(new ContactCommand(Atc.eType.app));

      routeName = "(vfr)";
    }
    Squawk sqwk = generateSqwk();
    spd = pt.vCruise;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        routeName, routeCmds);

    return ret;
  }

  private int generateArrivingPlaneAltitude(Route r) {
    double thousandsFeetPerMile = 0.30;

    double dist = r.getRouteLength();
    if (dist < 0) {
      dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());
    }

    int ret = (int) (dist * thousandsFeetPerMile) + rnd.nextInt(1, 5); //5, 12);
    ret = ret * 1000;
    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += rnd.nextDouble() * 50 - 25; // nahodne zatoceni priletoveho radialu
    double dist = rnd.nextDouble() * Global.MAX_ARRIVING_PLANE_DISTANCE + 5; // vzdalenost od prvniho bodu STARu
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

  private Route tryGetRandomIfrRoute(boolean isArrival, AirplaneType planeType) {

    Iterable<Route> rts = Acc.threshold().getRoutes();
    List<Route> avails = Routes.getByFilter(rts, isArrival, planeType.category);

    if (avails.isEmpty()) {
      return null; // if no route, return null
    }
    Route ret = eng.eSystem.Lists.getRandom(avails);

    return ret;
  }

  private Airplane generateNewDepartureAirplaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();
    AirplaneType pt = Acc.sim().getPlaneTypes().getRandomByTraffic(Acc.airport().getTrafficCategories(), m.isIfr());

    Route r;
    if (m.isIfr()) {
      r = tryGetRandomIfrRoute(false, pt);
    } else {
      r = null;
    }
    Coordinate coord = Acc.threshold().getCoordinate();
    Squawk sqwk = generateSqwk();

    int heading = (int) Acc.threshold().getCourse();
    int alt = Acc.threshold().getParent().getParent().getAltitude();
    int spd = 0;

    List<Command> routeCmds;
    if (r != null) {
      routeCmds = r.getCommandsListClone();
    } else {
      routeCmds = new ArrayList<>();
    }

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
    String routeName;
    if (r != null) {
      routeName = r.getName();
    } else {
      routeName = "(vfr)";
    }
    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, true,
        routeName, routeCmds);

    return ret;
  }

  private VfrPoint getRandomVfrEntryPoint(KeyList<VfrPoint, String> vfrPoints) {
    List<VfrPoint> entryPoints = new ArrayList();
    for (VfrPoint vfrPoint : vfrPoints) {
      if (vfrPoint.isForArrivals()) {
        entryPoints.add(vfrPoint);
      }
    }

    if (entryPoints.isEmpty()) {
      throw new ERuntimeException("No VFR entry points specified for the area. Cannot create VFR arrival traffic.");
    }

    VfrPoint ret = eng.eSystem.Lists.getRandom(entryPoints);
    return ret;
  }

}

