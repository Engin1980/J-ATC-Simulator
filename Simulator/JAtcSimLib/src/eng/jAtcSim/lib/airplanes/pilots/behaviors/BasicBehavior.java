package eng.jAtcSim.lib.airplanes.pilots.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.PassingClearanceLimitNotification;
import eng.jAtcSim.lib.world.Navaid;

import static eng.jAtcSim.lib.airplanes.pilots.Pilot.SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;

public abstract class BasicBehavior extends DivertableBehavior {
  private boolean clearanceLimitWarningSent = false;

  abstract void _fly(IPilot5Behavior pilot);

  @Override
  public final void fly(IPilot5Behavior pilot) {
    Coordinate targetCoordinate = pilot.getPlane().getSha().tryGetTargetCoordinate();
    if (targetCoordinate != null) {

      double warningDistance = pilot.getPlane().getSha().getSpeed() * .02;
      double overNavaidDistance = pilot.getPlane().getSha().getSpeed() * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;

      double dist = Coordinates.getDistanceInNM(pilot.getPlane().getCoordinate(), targetCoordinate);
      if (!clearanceLimitWarningSent && dist < warningDistance && !pilot.getRoutingModule().hasLateralDirectionAfterCoordinate()) {
        pilot.say(new PassingClearanceLimitNotification());
        clearanceLimitWarningSent = true;
      } else if (dist < overNavaidDistance) {
        if (pilot.getPlane().getFlight().isArrival() == false) {
          Navaid n = pilot.getRoutingModule().getAssignedRoute().getMainNavaid();
          dist = Coordinates.getDistanceInNM(pilot.getPlane().getCoordinate(), n.getCoordinate());
          if (dist < 1.5) {
            int rad = (int) Coordinates.getBearing(Acc.airport().getLocation(), n.getCoordinate());
            rad = rad % 90;
            pilot.setHoldBehavior(n, rad, true);
            return;
          }
        } else {
          pilot.setTargetCoordinate(null);
          clearanceLimitWarningSent = false;
        }
      } else {
        double heading = Coordinates.getBearing(pilot.getPlane().getCoordinate(), targetCoordinate);
        heading = Headings.to(heading);
        if (heading != pilot.getPlane().getSha().getTargetHeading()) {
          pilot.setTargetHeading(heading);
        }
      }
    }
    _fly(pilot);
  }
}
