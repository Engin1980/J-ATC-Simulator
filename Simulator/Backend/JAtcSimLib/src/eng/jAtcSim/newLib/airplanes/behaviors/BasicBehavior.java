package eng.jAtcSim.newLib.airplanes.behaviors;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.global.Headings;
import eng.jAtcSim.newLib.speaking.fromAirplane.notifications.PassingClearanceLimitNotification;
import eng.jAtcSim.newLib.world.Navaid;

public abstract class BasicBehavior extends DivertableBehavior {
  private boolean clearanceLimitWarningSent = false;

  abstract void _fly(IAirplaneWriteSimple plane);

  @Override
  public final void fly(IAirplaneWriteSimple plane) {
    Coordinate targetCoordinate = plane.getSha().tryGetTargetCoordinate();
    if (targetCoordinate != null) {

      double warningDistance = plane.getSha().getSpeed() * .02;
      double overNavaidDistance = Navaid.getOverNavaidDistance(plane.getSha().getSpeed());

      double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), targetCoordinate);
      if (!clearanceLimitWarningSent && dist < warningDistance && !plane.getRoutingModule().hasLateralDirectionAfterCoordinate()) {
        plane.sendMessage(new PassingClearanceLimitNotification());
        clearanceLimitWarningSent = true;
      } else if (dist < overNavaidDistance) {
        if (plane.getFlightModule().isArrival() == false) {
          Navaid n = plane.getRoutingModule().getAssignedRoute().getMainNavaid();
          dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
          if (dist < 1.5) {
            int rad = (int) Coordinates.getBearing(Acc.airport().getLocation(), n.getCoordinate());
            rad = rad % 90;
            plane.getAdvanced().hold(n, rad, true);
            return;
          }
        } else {
          plane.setTargetCoordinate(null);
          clearanceLimitWarningSent = false;
        }
      } else {
        double heading = Coordinates.getBearing(plane.getCoordinate(), targetCoordinate);
        heading = Headings.to(heading);
        if (heading != plane.getSha().getTargetHeading()) {
          plane.setTargetHeading(heading);
        }
      }
    }
    _fly(plane);
  }
}
