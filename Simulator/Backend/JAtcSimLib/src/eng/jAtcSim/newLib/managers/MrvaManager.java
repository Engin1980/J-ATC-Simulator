package eng.jAtcSim.newLib.area.managers;

import eng.eSystem.collections.*;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplane4Mrva;
import eng.jAtcSim.newLib.world.Border;

public class MrvaManager {

  private IReadOnlyList<Border> mrvas;
  private IMap<IAirplane4Mrva, Border> maps = new EMap<>();

  public MrvaManager(IReadOnlyList<Border> mrvas) {
    assert mrvas.isAll(q -> q.getType() == Border.eType.mrva);
    assert mrvas.isAll(q -> q.isEnclosed());
    this.mrvas = mrvas;
  }

  public void registerPlane(IAirplane4Mrva plane) {
    maps.set(plane, null);
  }

  public void unregisterPlane(IAirplane4Mrva plane) {
    maps.remove(plane);
  }

  public void evaluateMrvaFails() {
    for (IAirplane4Mrva airplane : maps.getKeys()) {
      evaluateMrvaFail(airplane);
    }
  }

  private void evaluateMrvaFail(IAirplane4Mrva airplane) {
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
      airplane.setMrvaError(false);
      if (maps.get(airplane) != null) maps.set(airplane, null);
    } else {
      Border m = maps.get(airplane);
      boolean findNewOne = false;
      if (m == null && airplane.getSha().getVerticalSpeed() <= 0)
        findNewOne = true;
      else if (m != null && m.isIn(airplane.getCoordinate()) == false)
        findNewOne = true;
      if (findNewOne) {
        m = this.mrvas.tryGetFirst(q -> q.isIn(airplane.getCoordinate()));
        maps.set(airplane, m);
      }

      boolean isOutOfAltitude = false;
      if (m != null) isOutOfAltitude = m.isIn(airplane.getSha().getAltitude());
      if (isOutOfAltitude && airplane.getState().is(AirplaneState.arrivingLow, AirplaneState.departingLow)) {
        // this is for departures/goarounds when close to runway, very low, so are omitted
        double d = Coordinates.getDistanceInNM(airplane.getCoordinate(), Acc.airport().getLocation());
        if (d < 3)
          isOutOfAltitude = false;
      }
      airplane.setMrvaError(isOutOfAltitude);
    }
  }
}
