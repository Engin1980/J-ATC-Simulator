/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Routes;

import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class TestTrafficOneApproach extends TestTraffic {

  Airplane a = null;
  boolean done = false;

  private Airplane generatePlane() {
    Airplane ret;

    AirplaneType pt = Acc.sim().getPlaneTypes().getRandomByTraffic(Acc.airport().getTrafficCategories(), true);
    Route r = tryGetRandomRoute(true, pt);
    //Coordinate coord = new Coordinate(50.217994, 14.307286);
    //int heading = 065;

    Coordinate coord = generateArrivalCoordinate(r.getMainFix().getCoordinate(), Acc.threshold().getCoordinate());
    int heading = (int) Coordinates.getBearing(coord, r.getMainFix().getCoordinate());
    int alt = 7000; // generateArrivingPlaneAltitude(r);
    int spd = pt.vCruise;
    SpeechList<IAtcCommand> routeCmds = r.getCommandsListClone();
    // added command to descend
    routeCmds.add(0,
      new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.descend,
        Acc.atcCtr().getOrderedAltitude()
      ));
    // added command to contact CTR
    routeCmds.add(0, new ContactCommand(Atc.eType.ctr));

    ret = new Airplane(
      new Callsign("CSA", "1111"),
      coord,
      Squawk.create("1111"),
      pt,
      heading,
      alt,
      spd,
      false,
      r.getName(),
      routeCmds);

    return ret;
  }

  private Route tryGetRandomRoute(boolean arrival, AirplaneType planeType) {

    Iterable<Route> rts = Acc.threshold().getRoutes();
    List<Route> avails = Routes.getByFilter(rts, arrival, planeType.category);

    if (avails.isEmpty()) {
      return null; // if no route, return null
    }
    int index = Simulation.rnd.nextInt(avails.size());

    Route ret = avails.get(index);

    return ret;
  }

  private int generateArrivingPlaneAltitude(Route r) {
    double thousandsFeetPerMile = 0.30;

    double dist = r.getRouteLength();
    if (dist < 0) {
      dist = Coordinates.getDistanceInNM(r.getMainFix().getCoordinate(), Acc.airport().getLocation());
    }

    int ret = (int) (dist * thousandsFeetPerMile) + Simulation.rnd.nextInt(1, 5); //5, 12);
    ret = ret * 1000;
    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Simulation.rnd.nextDouble() * 50 - 25; // nahodne zatoceni priletoveho radialu
    double dist = Simulation.rnd.nextDouble() * Global.MAX_ARRIVING_PLANE_DISTANCE; // vzdalenost od prvniho bodu STARu
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

  @Override
  public Airplane[] getNewAirplanes() {
    if (a != null) {
      Airplane[] ret = new Airplane[]{a};
      a = null;
      return ret;
    } else {
      return new Airplane[0];
    }
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (!done) {
      a = generatePlane();
      done = true;
    }
  }

  @Override
  public Movement[] getScheduledMovements() {
    return new Movement[0];
  }

}
