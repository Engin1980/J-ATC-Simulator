/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area.airplanes;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.utilites.StringUtils;
import eng.jAtcSim.newLib.Acc;

import java.util.List;

/**
 * @author Marek
 */
public class Airplanes {

  private static final double AIRPROX_STANDARD_DISTANCE = 5;
  private static final double AIRPROX_WARNING_DISTANCE = 7.5;
  private static final double AIRPROX_APPROACH_DISTANCE = 2.5;
  private static final Airplane.State[] airproxApproachStates = {
      Airplane.State.flyingIaf2Faf,
      Airplane.State.approachEnter,
      Airplane.State.approachDescend,
      Airplane.State.longFinal,
      Airplane.State.shortFinal,
      Airplane.State.landed,
      Airplane.State.takeOffRoll,
      Airplane.State.takeOffGoAround
  };

  public static Airplane tryGetByCallsingOrNumber(Iterable<Airplane> planes, String callsignOrNumber) {
    if (StringUtils.isNullOrEmpty(callsignOrNumber)) return null;

    Airplane ret = CollectionUtils.tryGetFirst(planes, p -> p.getFlightModule().getCallsign().toString(false).equals(callsignOrNumber));
    if (ret == null) {
      List<Airplane> byPart = CollectionUtils.where(planes,
          p -> p.getFlightModule().getCallsign().getNumber().equals(callsignOrNumber));
      if (byPart.size() == 1)
        ret = byPart.get(0);
    }

    return ret;
  }

  public static Airplane tryGetByCallsign(IReadOnlyList<Airplane> planes, String callsign) {
    Callsign cs;
    try {
      cs = new Callsign(callsign);
    } catch (Exception ex) {
      return null;
    }
    return tryGetByCallsign(planes, cs);
  }

  public static Airplane tryGetByCallsign(IReadOnlyList<Airplane> planes, Callsign callsign) {
    return planes.tryGetFirst(q -> q.getFlightModule().getCallsign().equals(callsign));
  }

  public static Airplane getByCallsing(IReadOnlyList<Airplane> planes, String callsign) {
    Callsign cs = new Callsign(callsign);
    return getByCallsing(planes, cs);
  }

  public static Airplane getByCallsing(IReadOnlyList<Airplane> planes, Callsign callsign) {
    return planes.getFirst(q -> q.getFlightModule().getCallsign().equals(callsign));
  }

  public static Airplane tryGetBySqwk(IReadOnlyList<Airplane> planes, String sqwk) {
    Squawk s;
    try {
      s = Squawk.create(sqwk);
    } catch (Exception ex) {
      return null;
    }
    return Airplanes.tryGetBySqwk(planes, s);
  }

  public static Airplane tryGetBySqwk(IReadOnlyList<Airplane> planes, Squawk sqwk) {
    Airplane ret = planes.tryGetFirst(q -> q.getSqwk().equals(sqwk));
    return ret;
  }

  public static void evaluateAirproxes(IReadOnlyList<Airplane> planes) {
    for (Airplane p : planes) {
      p.resetAirprox();
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

        a.increaseAirprox(at);
        b.increaseAirprox(at);
      }
    }
  }

  private static AirproxType isInAirprox(Airplane a, Airplane b) {
    AirproxType ret;

    double alt = Math.abs(a.getSha().getAltitude() - b.getSha().getAltitude());

    if (alt >= 1500) {
      return AirproxType.none;
    }

    double dist = Coordinates.getDistanceInNM(
        a.getCoordinate(), b.getCoordinate());

    if (alt < 950) {
      boolean isAinApp = a.getState().is(airproxApproachStates);
      boolean isBinApp = b.getState().is(airproxApproachStates);
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
      if (dist < AIRPROX_STANDARD_DISTANCE) {
        if (a.getSha().getAltitude() == b.getSha().getAltitude())
          ret = AirproxType.warning;
        else if ((a.getSha().getAltitude() > b.getSha().getAltitude() && a.getSha().getVerticalSpeed() < 0 && b.getSha().getVerticalSpeed() > 0)
          || (a.getSha().getAltitude() < b.getSha().getAltitude() && a.getSha().getVerticalSpeed() > 0 && b.getSha().getVerticalSpeed() < 0))
          ret = AirproxType.warning;
        else
          ret = AirproxType.none;
      } else
        ret = AirproxType.none;
    }

    return ret;
  }

}
