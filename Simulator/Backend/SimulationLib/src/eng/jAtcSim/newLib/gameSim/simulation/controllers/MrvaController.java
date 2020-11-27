package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.Callsign;

public class MrvaController {
  private final IReadOnlyList<Border> mrvas;
  private final IMap<IAirplane, Border> mrvaMaps = new EMap<>();
  private final IList<Callsign> mrvaViolatingPlanes = new EList<>();

  public MrvaController(IReadOnlyList<Border> mrvas) {
    assert mrvas.isAll(q -> q.getType() == Border.eType.mrva);
    assert mrvas.isAll(q -> q.isEnclosed());
    this.mrvas = mrvas;
  }

  public void evaluateMrvaFails(IReadOnlyList<IAirplane> planes) {
    this.mrvaViolatingPlanes.clear();
    for (IAirplane plane : planes) {
      evaluateMrvaFail(plane);
    }
  }

  public IReadOnlyList<Callsign> getMrvaViolatingPlanes() {
    return mrvaViolatingPlanes;
  }

  public boolean isMrvaErrorForPlane(IAirplane airplane) {
    return this.mrvaViolatingPlanes.isAny(q -> q.equals(airplane.getCallsign()));
  }

  private void evaluateMrvaFail(IAirplane airplane) {
    if (airplane.getState().is(
            AirplaneState.holdingPoint,
            AirplaneState.takeOffRoll,
            AirplaneState.takeOffGoAround,
            AirplaneState.flyingIaf2Faf,
            AirplaneState.approachEnter,
            AirplaneState.approachDescend,
            AirplaneState.longFinal,
            AirplaneState.shortFinal,
            AirplaneState.landed
    )) {
      mrvaMaps.tryRemove(airplane);
    } else {
      Border m = mrvaMaps.tryGet(airplane, null);
      boolean findNewOne = false;
      if (m == null && airplane.getSha().getVerticalSpeed() <= 0)
        findNewOne = true;
      else if (m != null && m.isIn(airplane.getCoordinate()) == false)
        findNewOne = true;
      if (findNewOne) {
        m = this.mrvas.tryGetFirst(q -> q.isIn(airplane.getCoordinate()));
        if (m != null) mrvaMaps.set(airplane, m);
      }

      boolean isOutOfAltitude = false;
      if (m != null) isOutOfAltitude = m.isIn(airplane.getSha().getAltitude());
      if (isOutOfAltitude && airplane.getState().is(AirplaneState.arrivingLow, AirplaneState.departingLow)) {
        // this is for departures/goarounds when close to runway, very low, so are omitted
        double d = Coordinates.getDistanceInNM(airplane.getCoordinate(), Context.getArea().getAirport().getLocation());
        if (d < 3)
          isOutOfAltitude = false;
      }
      if (isOutOfAltitude)
        mrvaViolatingPlanes.add(airplane.getCallsign());
    }
  }
}
