package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.IReadOnlyMap;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.AtcType;

public class AirproxController {
  private static final double AIRPROX_STANDARD_DISTANCE = 5;
  private static final double AIRPROX_WARNING_DISTANCE = 7.5;
  private static final double AIRPROX_APPROACH_DISTANCE = 2.5;
  private static final AirplaneState[] airproxApproachStates = {
      AirplaneState.flyingIaf2Faf,
      AirplaneState.approachEnter,
      AirplaneState.approachDescend,
      AirplaneState.longFinal,
      AirplaneState.shortFinal,
      AirplaneState.landed,
      AirplaneState.takeOffRoll,
      AirplaneState.takeOffGoAround
  };

  private static AirproxType getAirproxLevel(IAirplane a, IAirplane b) {
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
  private final IMap<Callsign, AirproxType> airproxViolatingPlanes = new EMap<>();

  public void evaluateAirproxFails() {
    airproxViolatingPlanes.clear();

    IReadOnlyList<IAirplane> planes = Context.getAirplane().getAirplanes();

    for (int i = 0; i < planes.count() - 1; i++) {
      IAirplane a = planes.get(i);
      if (a.getState().is(
          AirplaneState.holdingPoint,
          AirplaneState.landed,
          AirplaneState.takeOffRoll
      )) continue;
      for (int j = i + 1; j < planes.size(); j++) {
        IAirplane b = planes.get(j);
        if (b.getState().is(
            AirplaneState.holdingPoint,
            AirplaneState.landed,
            AirplaneState.takeOffRoll
        )) continue;

        AirproxType at = getAirproxLevel(a, b);

        if (at == AirproxType.full && Context.getAtc().getResponsibleAtcId(a.getCallsign()).getType() != AtcType.app &&
            Context.getAtc().getResponsibleAtcId(b.getCallsign()).getType() != AtcType.app)
          at = AirproxType.partial;

        storeAirprox(a.getCallsign(), at);
        storeAirprox(b.getCallsign(), at);
      }
    }
  }

  public IReadOnlyMap<Callsign, AirproxType> getAirproxViolatingPlanes() {
    return airproxViolatingPlanes;
  }

  private void storeAirprox(Callsign callsign, AirproxType airproxType) {
    if (airproxType == AirproxType.none) return;
    AirproxType current = this.airproxViolatingPlanes.tryGet(callsign, AirproxType.none);
    current = AirproxType.combine(current, airproxType);
    this.airproxViolatingPlanes.set(callsign, current);
  }

}
