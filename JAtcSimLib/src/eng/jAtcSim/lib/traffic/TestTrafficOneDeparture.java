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
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Routes;

import java.util.List;

/**
 *
 * @author Marek Vajgl
 */
public class TestTrafficOneDeparture extends TestTraffic {

  Airplane a = null;
  Airplane b = null;
  Airplane c = null;
  boolean done = false;

  private Airplane generatePlane(String number) {

    Airplane ret;

    Callsign cs;
    cs = new Callsign("CSA", number);
    AirplaneType pt = Acc.sim().getPlaneTypes().tryGetByName("A319");
    assert pt != null;

    Route r;
    Iterable<Route> rts = Acc.threshold().getRoutes();
    List<Route> avails = Routes.getByFilter(rts, false, pt.category);
    if (avails.isEmpty()) {
      return null; // if no route, return null
    }
    r = eng.eSystem.Lists.getRandom(avails);

    Coordinate coord = Acc.threshold().getCoordinate();
    Squawk sqwk = Squawk.create(number);

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

  @Override
  public Airplane[] getNewAirplanes() {
    if (a != null) {
      Airplane[] ret = new Airplane[]{a, b,c};
      a = null;
      b = null;
      c = null;
      return ret;
    } else {
      return new Airplane[0];
    }
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (!done) {
      a = generatePlane("0000");
      b = generatePlane("7777");
      c = generatePlane("5555");
      done = true;
    }
  }

  @Override
  public Movement[] getScheduledMovements() {
    return new Movement[0];
  }

}
