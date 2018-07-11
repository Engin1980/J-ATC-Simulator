package eng.jAtcSim.lib.managers;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.NumberUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.logging.CommonRecorder;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.world.Border;
import eng.jAtcSim.lib.world.BorderExactPoint;
import eng.jAtcSim.lib.world.BorderPoint;

public class MrvaManager {

  private IReadOnlyList<Border> mrvas;
  private IMap<Airplane, Border> maps = new EMap<>();

  public MrvaManager(IReadOnlyList<Border> mrvas) {
    assert mrvas.isAll(q->q.getType() == Border.eType.mrva);
    assert mrvas.isAll(q->q.isEnclosed());
    this.mrvas = mrvas;
  }

  public void registerPlane(Airplane plane) {
    maps.set(plane, null);
  }

  public void unregisterPlane(Airplane plane) {
    maps.remove(plane);
  }

  public void evaluateMrvaFails() {
    for (Airplane airplane : maps.getKeys()) {
      evaluateMrvaFail(airplane);
    }
  }

  private void evaluateMrvaFail(Airplane airplane) {
    if (airplane.getState().is(
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    )) {
      airplane.setMrvaError(false);
      if (maps.get(airplane) != null) maps.set(airplane, null);
    } else {
      Border m = maps.get(airplane);
      boolean findNewOne = false;
      if (m == null && airplane.getVerticalSpeed() <= 0)
        findNewOne = true;
      else if (m != null && m.isIn(airplane.getCoordinate()) == false)
        findNewOne = true;
      if (findNewOne) {
        m = this.mrvas.tryGetFirst(q -> q.isIn(airplane.getCoordinate()));
        maps.set(airplane, m);
      }

      boolean isOutOfAltitude = false;
      if (m != null) isOutOfAltitude = m.isIn(airplane.getAltitude());
      if (isOutOfAltitude && airplane.getState().is(Airplane.State.arrivingLow, Airplane.State.departingLow)) {
        // this is for departures/goarounds when close to runway, very low, so are omitted
        double d = Coordinates.getDistanceInNM(airplane.getCoordinate(), Acc.airport().getLocation());
        if (d < 3)
          isOutOfAltitude = false;
      }
      airplane.setMrvaError(isOutOfAltitude);
    }
  }
}
