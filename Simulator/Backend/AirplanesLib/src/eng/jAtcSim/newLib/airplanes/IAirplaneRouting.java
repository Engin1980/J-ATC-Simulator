package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.Navaid;

public interface IAirplaneRouting {
  Navaid getEntryExitPoint();

  boolean hasLateralDirectionAfterCoordinate();

  boolean isDivertable();

  boolean isGoingToFlightOverNavaid(Navaid n);

  boolean isRoutingEmpty();

  Coordinate tryGetTargetCoordinate();

  /*
if (targetCoordinate == null
      && parent.getBehaviorModule().is(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class)) {
    eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior hb = parent.getBehaviorModule().getAs(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class);
    targetCoordinate = hb.navaid.getCoordinate();
  }
 */
  Coordinate tryGetTargetOrHoldCoordinate();
}
