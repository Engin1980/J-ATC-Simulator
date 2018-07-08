package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.LinkedList;
import java.util.List;

public class TrafficManager {
  public static double MAX_ARRIVING_PLANE_DISTANCE = 30;
  public static double MIN_ARRIVING_PLANE_DISTANCE = 15;

  private IList<Movement> scheduledMovements = new EList<>();
  private Traffic traffic;
  private Object lastRelativeInfo;
  private ETime nextGenerateTime = new ETime(0);

  public void generateNewTrafficIfRequired() {
    if (Acc.now().isAfterOrEq(nextGenerateTime)) {
      GeneratedMovementsResponse gmr = traffic.generateMovements(lastRelativeInfo);
      this.scheduledMovements.add(gmr.getNewMovements());
      this.scheduledMovements.sort(q -> q.getInitTime()); // Movement has inner class comparer
      this.nextGenerateTime = gmr.getNextTime();
      this.lastRelativeInfo = gmr.getSyncObject();
    }
  }

  /**
   * Returns new airplanes antecedent specified time.
   *
   * @return New airplanes
   */
  public final IReadOnlyList<Airplane> getNewAirplanes() {

    IList<Airplane> ret = new EList<>();

    IList<Movement> readyMovements = scheduledMovements.where(q->q.getInitTime().isBeforeOrEq(Acc.now()));
    scheduledMovements.remove(readyMovements);
    for (Movement readyMovement : readyMovements) {
      Airplane a = this.convertMovementToAirplane(readyMovement);
      if (a == null){
        Acc.messenger().send(new Message(Messenger.SYSTEM, Acc.atcApp(),
            new StringMessageContent("Flight " + readyMovement.getCallsign() + " IFR flight plan canceled, no route.")));
      } else
        ret.add(a);
    }

    return ret;
  }

  public final IReadOnlyList<Movement> getScheduledMovements() {
    IReadOnlyList<Movement> ret = scheduledMovements;
    return ret;
  }

  public void setTraffic(Traffic traffic) {
    this.traffic = traffic;
  }

  public void throwOutElapsedMovements(ETime minTime) {
    scheduledMovements.remove(q->q.getInitTime().isBefore(minTime));
  }

  private Airplane convertMovementToAirplane(Movement m) {
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
    if (route == null) return null; // no route means disallowed IFR
    Coordinate coord = Acc.airport().getLocation();
    Squawk sqwk = generateSqwk();

    int heading = 0;
    int alt = Acc.airport().getAltitude();
    int spd = 0;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, true,
        route, m.getDelayInMinutes(), m.getInitTime().addMinutes(3));

    return ret;
  }

  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();

    AirplaneType pt = m.getAirplaneType();

    Route route;
    Coordinate coord;
    int heading;
    int alt;
    int spd;

    route = tryGetRandomIfrRoute(true, pt);
    if (route == null) {
      return null; // no route means disallowed IFR
    }
    coord = generateArrivalCoordinate(route.getEntryFix().getCoordinate(), Acc.airport().getLocation());
    heading = (int) Coordinates.getBearing(coord, route.getEntryFix().getCoordinate());
    alt = generateArrivingPlaneAltitude(route, pt);

    Squawk sqwk = generateSqwk();
    spd = pt.vCruise;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        route, m.getDelayInMinutes(), m.getInitTime().addMinutes(25));

    return ret;
  }

  private int generateArrivingPlaneAltitude(Route r, AirplaneType type) {

    int ret;
    if (r.getEntryFL() != null) {
      ret = r.getEntryFL() * 100;
    } else {
      double thousandsFeetPerMile = 500;
      double dist = r.getRouteLength();
      if (dist <= 0) {
        dist = Coordinates.getDistanceInNM(r.getEntryFix().getCoordinate(), Acc.airport().getLocation());
      }
      ret = (int) (dist * thousandsFeetPerMile);
    }

    // update by random value
    ret += Acc.rnd().nextInt(-3000, 5000);
    if (ret > type.maxAltitude) {
      if (ret < 12000)
        ret = type.maxAltitude - Acc.rnd().nextInt(4) * 1000;
      else if (ret < 20000)
        ret = type.maxAltitude - Acc.rnd().nextInt(7) * 1000;
      else
        ret = type.maxAltitude - Acc.rnd().nextInt(11) * 1000;
    }
    ret = ret / 1000 * 1000;

    // check if initial altitude is not below STAR mrva
    if (ret < r.getMaxMrvaAltitude()){
      double tmp = Math.ceil(r.getMaxMrvaAltitude() / 10d) * 10;
      ret = (int) tmp;
    }

    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Simulation.rnd.nextDouble(-25, 25); // nahodne zatoceni priletoveho radialu
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
    String[] illegals = new String[]{
        "7500", "7600", "7700"
    };

    int len = 4;
    char[] tmp;
    Squawk ret = null;
    while (ret == null) {
      tmp = new char[len];
      for (int i = 0; i < len; i++) {
        tmp[i] = Integer.toString(Simulation.rnd.nextInt(8)).charAt(0);
      }
      if (ArrayUtils.contains(illegals, tmp.toString()))
        continue;
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

//  private Route generatePointRoute(boolean arrival) {
//    List<Navaid> nvs = new LinkedList();
//
//    for (Runway rw : Acc.airport().getRunways()) {
//      for (RunwayThreshold rt : rw.getThresholds()) {
//        for (Route r : rt.getRoutes()) {
//          if (arrival && r.getType() == Route.eType.sid) {
//            continue;
//          } else if (!arrival && r.getType() != Route.eType.sid) {
//            continue;
//          }
//
//          Navaid n = r.getMainFix();
//
//          if (nvs.contains(n) == false) {
//            nvs.add(n);
//          }
//        }
//      }
//    }
//
//    int index = Acc.rnd().nextInt(nvs.size());
//
//    Navaid n = nvs.get(index);
//
//    Route r = Route.createNewByFix(n, arrival);
//
//    return r;
//  }

  private Route tryGetRandomIfrRoute(boolean isArrival, AirplaneType planeType) {

    IList<Route> rts = new EList<>();

    IList<RunwayThreshold> thresholds;
    if (!isArrival) {
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
      rts = rts.where(q -> q.getType() != Route.eType.sid);
    else
      rts = rts.where(q -> q.getType() == Route.eType.sid);

    rts = rts.where(q->q.getMaxMrvaAltitude() < planeType.maxAltitude);

    rts = rts.where(q -> q.isValidForCategory(planeType.category));

    Route ret;

    if (rts.isEmpty())
      ret = null;
    else
      ret = rts.getRandom();

    return ret;
  }
}
