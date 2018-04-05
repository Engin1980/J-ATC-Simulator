/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.world.*;
import sun.security.x509.IssuingDistributionPointExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marek Vajgl
 */
public abstract class Traffic {

  private static final double COMPANY_THREE_CHAR_NUMBER_PROBABILITY = 0.3;
  private static final double EXTENDED_CALLSIGN_PROBABILITY = 0.3;
  public static double MAX_ARRIVING_PLANE_DISTANCE = 30;
  public static double MIN_ARRIVING_PLANE_DISTANCE = 15;
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
   * Returns new airplanes antecedent specified time.
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

    Route route = tryGetRandomIfrRoute(false, pt);
    Coordinate coord = Acc.airport().getLocation();
    Squawk sqwk = generateSqwk();

    int heading = 0;
    int alt = Acc.airport().getAltitude();
    int spd = 0;

    SpeechList<IAtcCommand> initialCommands = new SpeechList<>();

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, true,
        route, initialCommands, m.getDelayInMinutes(), m.getInitTime().addMinutes(3));

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
    SpeechList<IAtcCommand> initialCommands;

    r = tryGetRandomIfrRoute(true, pt);
    if (r == null) {
      r = generatePointRoute(true);
    }
    coord = generateArrivalCoordinate(r.getMainFix().getCoordinate(), Acc.airport().getLocation());
    heading = (int) Coordinates.getBearing(coord, r.getMainFix().getCoordinate());
    alt = generateArrivingPlaneAltitude(r, pt);

    initialCommands = new SpeechList<>();
    // added command to descend
    //TODO following should say ctr ATC, not this here
    initialCommands.add(new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.descend,
        Acc.atcCtr().getOrderedAltitude()
    ));

    Squawk sqwk = generateSqwk();
    spd = pt.vCruise;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        r, initialCommands, m.getDelayInMinutes(), m.getInitTime().addMinutes(25));

    return ret;
  }

  private int generateArrivingPlaneAltitude(Route r, AirplaneType type) {

    int ret;
    if (r.getEntryFL() != null) {
      ret = r.getEntryFL() * 100;
    }
    else {
      double thousandsFeetPerMile = 500;
      double dist = r.getRouteLength();
      if (dist <= 0) {
        dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());
      }
      ret = (int) (dist * thousandsFeetPerMile);
    }

    // update by random value
    ret += Acc.rnd().nextInt(-3000, 5000 );
    if (ret > type.maxAltitude) {
      if (ret < 12000)
        ret = type.maxAltitude - Acc.rnd().nextInt(4) * 1000;
      else if (ret < 20000)
        ret = type.maxAltitude - Acc.rnd().nextInt(7) * 1000;
      else
        ret = type.maxAltitude - Acc.rnd().nextInt(11) * 1000;
    }
    ret = ret / 1000 * 1000;
    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Simulation.rnd.nextDouble(-25,25); // nahodne zatoceni priletoveho radialu
    double dist = Simulation.rnd.nextDouble(MIN_ARRIVING_PLANE_DISTANCE, MAX_ARRIVING_PLANE_DISTANCE); // vzdalenost od prvniho bodu STARu
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

    IList<Route> rts = new EList<>();

    IList<RunwayThreshold> thresholds;
    if (!isArrival){
      thresholds = Acc.thresholds();
    } else {
      // if is arrival, scheduled thresholds are taken into account
      thresholds = Acc.atcTwr().getRunwayThresholdsScheduled();
      if (thresholds.isEmpty())
        thresholds = Acc.thresholds();
    }

    for (RunwayThreshold threshold : thresholds) {
      rts.add(threshold.getRoutes());
    }
    if (isArrival)
      rts = rts.where(q->q.getType() != Route.eType.sid);
    else
      rts = rts.where(q->q.getType() == Route.eType.sid);

    rts = rts.where(q->q.isValidForCategory(planeType.category));

    Route ret;

    if (rts.isEmpty())
      ret = null;
    else
      ret = rts.getRandom();

    return ret;
  }

  protected Callsign generateUnusedCallsign(String companyOrCountryPrefix, boolean isPrefixCountryCode) {
    Callsign ret = null;

    while (ret == null) {

      ret = generateRandomCallsign(companyOrCountryPrefix, isPrefixCountryCode);
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

  private Callsign generateRandomCallsign(@Nullable String prefix, boolean isPrefixCountryCode) {
    String number;
    boolean useExtendedNow = this.useExtendedCallsigns && Acc.rnd().nextDouble() < EXTENDED_CALLSIGN_PROBABILITY;

    if (!isPrefixCountryCode) {
      int length = (Acc.rnd().nextDouble() < COMPANY_THREE_CHAR_NUMBER_PROBABILITY) ? 3 : 4;
      number = getRandomCallsignNumber(true, useExtendedNow, length);
    } else {
      number = getRandomCallsignNumber(false, true, 5 - prefix.length());
    }
    Callsign ret = new Callsign(prefix, number);
    return ret;
  }

  private String getRandomCallsignNumber(boolean useNumbers, boolean useChars, int length) {
    char[] tmp = new char[length];
    boolean isNumber = useNumbers;

    for (int i = 0; i < length; i++) {
      if (isNumber)
        tmp[i] = getRandomCallsignChar('0','9' );
      else
        tmp[i] = getRandomCallsignChar('A','Z' );
      if (useChars && useNumbers){
        if ((i+2) == length)
          isNumber = false;
        else if ((length == 4 && i == 1) || length == 3)
          isNumber = Math.random() > 0.5;
      }
    }

    String ret = new String(tmp);
    return ret;
  }

  private char getRandomCallsignChar(char from, char to) {
    int val = Acc.rnd().nextInt(from, to + 1);
    char ret = (char) val;
    return ret;
  }
}
