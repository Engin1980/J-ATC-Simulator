/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.utilites.StringUtil;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.KeyItems;

import java.util.List;

/**
 * @author Marek
 */
public class Airplanes {

  private static final double AIRPROX_STANDARD_DISTANCE = 5;
  private static final double AIRPROX_WARNING_DISTANCE = 7.5;
  private static final double AIRPROX_APPROACH_DISTANCE = 2.5;

  public static Airplane tryGetByCallsingOrNumber(Iterable<Airplane> planes, String callsignOrNumber) {
    if (StringUtil.isEmpty(callsignOrNumber)) return null;

    Airplane ret = CollectionUtils.tryGetFirst(planes, p -> p.getCallsign().toString(false).equals(callsignOrNumber));
    if (ret == null) {
      List<Airplane> byPart = CollectionUtils.where(planes,
          p -> p.getCallsign().getNumber().equals(callsignOrNumber));
      if (byPart.size() == 1)
        ret = byPart.get(0);
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

  public static void evaluateAirproxes(IReadOnlyList<Airplane> planes) {
    for (Airplane p : planes) {
      p.setAirprox(AirproxType.none);
    }

    for (int i = 0; i < planes.size() - 1; i++) {
      Airplane a = planes.get(i);
      if (a.getState().is(
          Airplane.State.holdingPoint,
          Airplane.State.landed,
          Airplane.State.takeOffRoll
      )) continue;
      for (int j = i + 1; j < planes.size(); j++) {
        Airplane b = planes.get(j);
        if (b.getState().is(
            Airplane.State.holdingPoint,
            Airplane.State.landed,
            Airplane.State.takeOffRoll
        )) continue;

        AirproxType at = isInAirprox(a, b);

        if (at == AirproxType.full && !(Acc.prm().getResponsibleAtc(a) == Acc.atcApp() &&
            Acc.prm().getResponsibleAtc(b) == Acc.atcApp()))
          at = AirproxType.partial;

        a.setAirprox(at);
        b.setAirprox(at);
      }
    }
  }

  private static AirproxType isInAirprox(Airplane a, Airplane b) {
    AirproxType ret;

    double alt = Math.abs(a.getAltitude() - b.getAltitude());

    if (alt >= 1500) {
      return AirproxType.none;
    }

    double dist = Coordinates.getDistanceInNM(
        a.getCoordinate(), b.getCoordinate());

    if (alt < 1000) {
      boolean isAinApp = a.getState().is(Airplane.State.approachDescend, Airplane.State.longFinal, Airplane.State.shortFinal, Airplane.State.landed, Airplane.State.takeOffRoll, Airplane.State.takeOffGoAround);
      boolean isBinApp = b.getState().is(Airplane.State.approachDescend, Airplane.State.longFinal, Airplane.State.shortFinal, Airplane.State.landed, Airplane.State.takeOffRoll, Airplane.State.takeOffGoAround);
      if (isAinApp && isBinApp) {
        if (dist < AIRPROX_APPROACH_DISTANCE) {
          ret = AirproxType.full;
        } else {
          ret = AirproxType.none;
        }
      } else {
        if (dist < AIRPROX_STANDARD_DISTANCE)
          ret = AirproxType.full;
        else if (dist < AIRPROX_WARNING_DISTANCE)
          ret = AirproxType.warning;
        else
          ret = AirproxType.none;
      }
    } else {
      if (dist < AIRPROX_STANDARD_DISTANCE)
        ret = AirproxType.warning;
      else
        ret = AirproxType.none;
    }

    return ret;
  }

}
