/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.KeyItems;
import eng.jAtcSim.lib.global.ReadOnlyList;
import eng.jAtcSim.lib.global.Strings;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.KeyItems;
import eng.jAtcSim.lib.global.ReadOnlyList;
import eng.jAtcSim.lib.global.Strings;

/**
 *
 * @author Marek
 */
public class Airplanes {

  public static Airplane tryGetByCallsingOrNumber(Iterable<Airplane> planes, String callsignOrNumber) {
    Airplane ret = null;
    if (Strings.isEmpty(callsignOrNumber)) return null;
    char f = callsignOrNumber.charAt(0);
    if (f >= '0' && f <= '9') {
      // only partial callsign
      for (Airplane p : planes) {
        if (p.getCallsign().getNumber().equals(callsignOrNumber)) {
          if (ret == null) {
            ret = p;
          } else {
            ret = null;
            break;
          }
        }
      }
    } else {
      // full callsign
      ret = Airplanes.tryGetByCallsign(planes, callsignOrNumber);
    }
    return ret;
  }

  public static Airplane tryGetByCallsign(Iterable<Airplane> planes, String callsign) {
    Callsign cs;
    try {
      cs = new Callsign(callsign);
    } catch (Exception ex) {
      return null;
    }
    return tryGetByCallsign(planes, cs);
  }

  public static Airplane tryGetByCallsign(Iterable<Airplane> planes, Callsign callsign) {
    return KeyItems.tryGet(planes, callsign);
  }

  public static Airplane getByCallsing(Iterable<Airplane> planes, String callsign) {
    Callsign cs = new Callsign(callsign);
    return getByCallsing(planes, cs);
  }

  public static Airplane getByCallsing(Iterable<Airplane> planes, Callsign callsign) {
    return KeyItems.get(planes, callsign);
  }

  public static Airplane tryGetBySqwk(Iterable<Airplane> planes, String sqwk) {
    Squawk s;
    try {
      s = Squawk.create(sqwk);
    } catch (Exception ex) {
      return null;
    }
    return Airplanes.tryGetBySqwk(planes, s);
  }

  public static Airplane tryGetBySqwk(Iterable<Airplane> planes, Squawk sqwk) {
    for (Airplane p : planes) {
      if (p.getSqwk().equals(sqwk)) {
        return p;
      }
    }
    return null;
  }

  public static void evaluateAirproxes(ReadOnlyList<Airplane> planes) {
    for (Airplane p : planes) {
      p.setAirprox(false);
    }

    for (int i = 0; i < planes.size() - 1; i++) {
      Airplane a = planes.get(i);
      if (a.getSpeed() == 0) continue;
      for (int j = i + 1; j < planes.size(); j++) {
        Airplane b = planes.get(j);
        if (b.getSpeed() == 0) continue;

        if (isInAirprox(a, b)) {
          a.setAirprox(true);
          b.setAirprox(true);
        }
      }

    }
  }

  private static boolean isInAirprox(Airplane a, Airplane b) {
    if (Math.abs(a.getAltitude() - b.getAltitude()) >= 1000) {
      return false;
    }

    double d = Coordinates.getDistanceInNM(
        a.getCoordinate(), b.getCoordinate());

    if (d > 5) {
      return false;
    }

    return true;
  }
}
