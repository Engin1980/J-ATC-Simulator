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
import jatcsimlib.airplanes.Airplanes;
import jatcsimlib.airplanes.Callsign;
import jatcsimlib.airplanes.Squawk;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.CentreAtc;
import jatcsimlib.atcs.TowerAtc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.commands.AfterAltitudeCommand;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.ChangeSpeedCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.global.ERandom;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Global;
import jatcsimlib.global.Headings;
import jatcsimlib.global.ReadOnlyList;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.weathers.Weather;
import jatcsimlib.weathers.WeatherDownloadNoaaGov;
import jatcsimlib.weathers.WeatherDownloader;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import jatcsimlib.world.Routes;
import jatcsimlib.world.Runway;
import jatcsimlib.world.RunwayThreshold;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Simulation {

  private final ETime now;
  private final AirplaneTypes planeTypes;
  private final Airport airport;

  private Weather weather;

  private final Messenger messenger = new Messenger();
  private final UserAtc appAtc;
  private final TowerAtc twrAtc;
  private final CentreAtc ctrAtc;

  private final EventManager<Simulation, EventListener<Simulation, Simulation>, Simulation> tickEM = new EventManager(this);

  public Airport getActiveAirport() {
    return airport;
  }

  public String toAltitudeString(int altInFt, boolean appendFt) {
    if (altInFt > getActiveAirport().getTransitionAltitude()) {
      return String.format("FL%03d", altInFt / 100);
    } else {
      if (appendFt) {
        return String.format("%04d ft", altInFt);
      } else {
        return String.format("%04d", altInFt);
      }
    }
  }

  public ETime getNow() {
    return now;
  }

  public ReadOnlyList<Airplane.AirplaneInfo> getPlaneInfos() {
    return Acc.prm().getInfos();
  }

  public Messenger getMessenger() {
    return messenger;
  }

  private Simulation(Airport airport, AirplaneTypes types, Calendar now) {
    if (airport == null) {
      throw new IllegalArgumentException("Argument \"airport\" cannot be null.");
    }

    this.airport = airport;
    this.planeTypes = types;
    this.twrAtc = new TowerAtc(airport.getAtcTemplates().get(Atc.eType.twr));
    this.ctrAtc = new CentreAtc(airport.getAtcTemplates().get(Atc.eType.ctr));
    this.appAtc = new UserAtc(airport.getAtcTemplates().get(Atc.eType.app));

    this.now = new ETime(now);
  }

  public static Simulation create(Airport airport, AirplaneTypes types, Calendar now) {
    Simulation ret = new Simulation(airport, types, now);

    Acc.setSimulation(ret);

    // weather
    WeatherDownloader wd = new WeatherDownloadNoaaGov();
    ret.weather = wd.downloadWeather(airport.getIcao());

    Acc.atcTwr().init();
    Acc.atcApp().init();
    Acc.atcCtr().init();

    return ret;
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

    this.ctrAtc.elapseSecond();
    this.twrAtc.elapseSecond();
    //this.appAtc.elapseSecond();

    generateNewPlanes();
    removeOldPlanes();
    updatePlanes();
    evalAirproxes();

    long end = System.currentTimeMillis();
    //System.out.println("## Sim elapse second: \t" + (end - start) + " at " + now.toString());

    tickEM.raise(this);
    isBusy = false;
  }

  private void updatePlanes() {
    for (Airplane plane : Acc.planes()) {
      plane.elapseSecond();
    }
  }

  private void generateNewPlanes() {
    if (Acc.planes().size() >= Global.MAX_PLANE_COUNT) { // smazat
      return;
    }

    if (this.now.getSeconds() % 5 != 0) {
      return;
    }

    Airplane plane;
    if (Acc.rnd().nextDouble() < Global.ARRIVING_PLANE_PROBABILITY) {
      plane = generateNewArrivingPlane();
    } else {
      plane = generateNewDepartingPlane();
    }

    if (plane.isDeparture()) {
      Acc.prm().registerPlane(twrAtc, plane);
      twrAtc.registerNewPlane(plane);
    } else {
      Acc.prm().registerPlane(ctrAtc, plane);
      ctrAtc.registerNewPlane(plane);
    }
  }

  public Atc getResponsibleAtc(Airplane plane) {
    return Acc.prm().getResponsibleAtc(plane);
  }

  private Airplane generateNewArrivingPlane() {
    Airplane ret;

    Callsign cs = generateCallsign();
    AirplaneType pt = planeTypes.getRandomByTraffic(Acc.airport().getTraffic());

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

  private Airplane generateNewDepartingPlane() {
    Airplane ret;

    Callsign cs = generateCallsign();
    AirplaneType pt = planeTypes.get(rnd.nextInt(planeTypes.size()));

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

  private Callsign generateCallsign() {
    Callsign ret = null;
    while (ret == null) {
      ret = new Callsign("CSA", String.format("%04d", rnd.nextInt(10000)));
      for (Airplane p : Acc.planes()) {
        if (ret.equals(p.getCallsign())) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }

  public static final ERandom rnd = new ERandom();

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

  private int generateArrivingPlaneAltitude(Route r) {
    double thousandsFeetPerMile = 0.30;

    double dist = r.getRouteLength();
    if (dist < 0)
      dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());

    int ret = (int) (dist * thousandsFeetPerMile) + rnd.nextInt(5, 12);
    ret = ret * 1000;
    return ret;
  }

  private void removeOldPlanes() {
    AirplaneList rem = new AirplaneList();
    for (Airplane p : Acc.planes()) {
      // landed
      if (p.isArrival() && p.getSpeed() < 30) {
        rem.add(p);
      }

      // departed
      if (p.isDeparture() && Acc.prm().getResponsibleAtc(p).equals(Acc.atcCtr())
          && (p.getAltitude() == p.getTargetAltitude() || p.getAltitude() > 18000)) {
        rem.add(p);
      }
    }

    for (Airplane p : rem) {
      Acc.prm().unregisterPlane(p);
    }
  }

  private void evalAirproxes() {
    Airplanes.evaluateAirproxes(Acc.planes());
  }

  public void setActiveRunwayThreshold(RunwayThreshold newRunwayThreshold) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public RunwayThreshold getActiveRunwayThreshold() {
    return Acc.threshold();
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

}
