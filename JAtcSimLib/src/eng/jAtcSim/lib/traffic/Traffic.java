/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.FleetType;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public abstract class Traffic {

  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.7;
  private String title;
  @XmlOptional
  private String description;
  /**
   * Specifies delay probability, range 0.0-1.0.
   */
  private final double delayProbability = 0.3;
  /**
   * Max delay in minutes per step.
   */
  private final int maxDelayInMinutesPerStep = 15;
  @XmlOptional
  private final List<Movement> scheduledMovements = new LinkedList();
  /**
   * Specifies if extended callsigns containing characters at the end can be used.
   */
  private boolean useExtendedCallsigns = true;

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Returns new airplanes after specified time.
   *
   * @return New airplanes
   */
  public final Airplane[] getNewAirplanes() {

    List<Airplane> ret = new ArrayList();
    while (scheduledMovements.size() > 0) {

      Movement m = scheduledMovements.get(0);
      if (m.getInitTime().isBeforeOrEq(Acc.now())) {

        //TODO this limit-check should be moved to simulation
        // check for too many planes. If true, movement is dropped
//        if (Acc.planes().size() + ret.size() < maxPlanesInSimulation) {
//          Airplane a = convertMovementToAirplane(m);
//          ret.add(a);
//        }

        scheduledMovements.remove(m);
        Airplane a = this.convertMovementToAirplane(m);
        ret.add(a);
      } else {
        break; // exit while if no more airplanes are ready
      }
    }

    Airplane[] retA = ret.toArray(new Airplane[0]);
    return retA;
  }

  public void setUseExtendedCallsigns(boolean useExtendedCallsigns) {
    this.useExtendedCallsigns = useExtendedCallsigns;
  }

  public final Movement[] getScheduledMovements() {
    Movement[] ret = scheduledMovements.toArray(new Movement[0]);
    return ret;
  }

  /**
   * Generates new airplanes for future, if required.
   */
  public abstract void generateNewMovementsIfRequired();

  protected void addScheduledMovement(Movement m) {
    this.scheduledMovements.add(m);
    Collections.sort(scheduledMovements, new Movement.SortByETimeComparer());
  }

  protected int generateDelayMinutes() {
    int ret = 0;
    while (Acc.rnd().nextDouble() < delayProbability) {
      int del = Acc.rnd().nextInt(maxDelayInMinutesPerStep);
      ret += del;
    }
    return ret;
  }

  protected Airplane convertMovementToAirplane(Movement m) {
    if (m.isDeparture()) {
      return generateNewDepartureAirplaneFromMovement(m);
    } else {
      return generateNewArrivalPlaneFromMovement(m);
    }
  }

  private Airplane generateNewDepartureAirplaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();
    AirplaneType pt = m.getAirplaneType();

    Route r = tryGetRandomIfrRoute(false, pt);
    Coordinate coord = Acc.threshold().getCoordinate();
    Squawk sqwk = generateSqwk();

    int heading = (int) Acc.threshold().getCourse();
    int alt = Acc.threshold().getParent().getParent().getAltitude();
    int spd = 0;

    SpeechList<IAtcCommand> routeCmds;
    if (r != null) {
      routeCmds = r.getCommandsListClone();
    } else {
      routeCmds = new SpeechList<>();
    }

    int indx = 0;
    routeCmds.add(indx++, new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.climb, Acc.threshold().getInitialDepartureAltitude()));

    // -- po vysce+300 ma kontaktovat APP
    routeCmds.add(indx++,
        new AfterAltitudeCommand(Acc.threshold().getParent().getParent().getAltitude() + Acc.rnd().nextInt(150, 450)));
    routeCmds.add(indx++, new ContactCommand(Atc.eType.app));

    // -- po vysce + 3000 rychlost na odlet
//    routeCmds.send(indx++,
//        new AfterAltitudeCommand(Acc.threshold().getParent().getParent().getAltitude() + 3000));
//    routeCmds.send(indx++, new ChangeSpeedCommand(ChangeSpeedCommand.eDirection.increase, 250));
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

  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();

    AirplaneType pt = m.getAirplaneType();

    Route r;
    Coordinate coord;
    int heading;
    int alt;
    int spd;
    SpeechList<IAtcCommand> routeCmds;
    String routeName;

    r = tryGetRandomIfrRoute(true, pt);
    if (r == null) {
      r = generatePointRoute(true);
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

    routeName = r.getName();

    Squawk sqwk = generateSqwk();
    spd = pt.vCruise;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        routeName, routeCmds);

    return ret;
  }

  private int generateArrivingPlaneAltitude(Route r) {
    double thousandsFeetPerMile = 500;

    double dist = r.getRouteLength();
    if (dist < 0) {
      dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());
    }

    int ret = (int) (dist * thousandsFeetPerMile) + Simulation.rnd.nextInt(-3000, 5000); //5, 12);
    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Simulation.rnd.nextDouble() * 50 - 25; // nahodne zatoceni priletoveho radialu
    double dist = Simulation.rnd.nextDouble() * Global.MAX_ARRIVING_PLANE_DISTANCE + 5; // vzdalenost od prvniho bodu STARu
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
      dist += Simulation.rnd.nextDouble() * 10;
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
        tmp[i] = Integer.toString(Simulation.rnd.nextInt(8)).charAt(0);
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

  private Route generatePointRoute(boolean arrival) {
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

  protected Callsign generateCallsign(String companyOrCountryPrefix, boolean isPrefixCountryCode) {
    Callsign ret = null;

    while (ret == null) {

      ret = buildRandomCallsign(companyOrCountryPrefix, isPrefixCountryCode);
      for (Airplane p : Acc.planes()) { // check not existing in current planes
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
      for (Movement m : this.getScheduledMovements()) { // check not existing in future planes
        if (m.getCallsign().equals(ret)) {
          ret = null;
          break;
        }
      }

    }

    return ret;
  }

  private Callsign buildRandomCallsign(@Nullable String prefix, boolean isPrefixCountryCode) {
    StringBuilder sb = new StringBuilder();
    if (!isPrefixCountryCode) {
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
}
