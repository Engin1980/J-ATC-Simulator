package eng.jAtcSim.lib.airplanes.pilots.behaviors;

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
    if (pilot.getTargetCoordinate() != null) {

      double warningDistance = pilot.getSpeed() * .02;
      double overNavaidDistance = pilot.getSpeed() * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;

      double dist = Coordinates.getDistanceInNM(pilot.getCoordinate(), pilot.getTargetCoordinate());
      if (!clearanceLimitWarningSent && dist < warningDistance && !pilot.hasLateralDirectionAfterCoordinate()) {
        pilot.say(new PassingClearanceLimitNotification());
        clearanceLimitWarningSent = true;
      } else if (dist < overNavaidDistance) {
        if (pilot.isArrival() == false) {
          Navaid n = pilot.getAssignedRoute().getMainNavaid();
          dist = Coordinates.getDistanceInNM(pilot.getCoordinate(), n.getCoordinate());
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
        double heading = Coordinates.getBearing(pilot.getCoordinate(), pilot.getTargetCoordinate());
        heading = Headings.to(heading);
        if (heading != pilot.getTargetHeading()) {
          pilot.setTargetHeading(heading);
        }
      }
    }
    _fly(pilot);
  }
}
