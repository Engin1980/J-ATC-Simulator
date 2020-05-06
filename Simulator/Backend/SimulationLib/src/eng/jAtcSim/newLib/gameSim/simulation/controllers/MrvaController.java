package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Border;
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

  public IReadOnlyList<Callsign> getMrvaViolatingPlanes() {
    return mrvaViolatingPlanes;
  }

  public void registerPlane(IAirplane plane) {
    mrvaMaps.set(plane, null);
  }

  public void unregisterPlane(IAirplane plane) {
    mrvaMaps.remove(plane);
  }

  public void evaluateMrvaFails() {
    this.mrvaViolatingPlanes.clear();
    for (IAirplane airplane : mrvaMaps.getKeys()) {
      evaluateMrvaFail(airplane);
    }
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
      if (mrvaMaps.get(airplane) != null) mrvaMaps.set(airplane, null);
    } else {
      Border m = mrvaMaps.get(airplane);
      boolean findNewOne = false;
      if (m == null && airplane.getSha().getVerticalSpeed() <= 0)
        findNewOne = true;
      else if (m != null && m.isIn(airplane.getCoordinate()) == false)
        findNewOne = true;
      if (findNewOne) {
        m = this.mrvas.tryGetFirst(q -> q.isIn(airplane.getCoordinate()));
        mrvaMaps.set(airplane, m);
      }

      boolean isOutOfAltitude = false;
      if (m != null) isOutOfAltitude = m.isIn(airplane.getSha().getAltitude());
      if (isOutOfAltitude && airplane.getState().is(AirplaneState.arrivingLow, AirplaneState.departingLow)) {
        // this is for departures/goarounds when close to runway, very low, so are omitted
        double d = Coordinates.getDistanceInNM(airplane.getCoordinate(), AreaAcc.getAirport().getLocation());
        if (d < 3)
          isOutOfAltitude = false;
      }
      if (isOutOfAltitude)
        mrvaViolatingPlanes.add(airplane.getCallsign());
    }
  }
}
