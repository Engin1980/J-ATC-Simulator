package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.airplane2atc.PassingClearanceLimitNotification;

public abstract class BasicPilot extends Pilot {

  private boolean clearanceLimitWarningSent = false;

  public BasicPilot(IPilotPlane plane) {
    super(plane);
  }

  @Override
  public final boolean isDivertable() {
    return true;
  }

  protected abstract void elapseSecondInternal();

  @Override
  public final void elapseSecond() {
    Coordinate targetCoordinate = plane.tryGetTargetCoordinate();
    if (targetCoordinate != null) {

      double warningDistance = plane.getSpeed() * .02;
      double overNavaidDistance = Navaid.getOverNavaidDistance(plane.getSpeed());

      double dist = Coordinates.getDistanceInNM(plane.getCoordinate(), targetCoordinate);
      if (!clearanceLimitWarningSent
          && dist < warningDistance
          && !plane.hasLateralDirectionAfterCoordinate()) {
        plane.sendMessage(new PassingClearanceLimitNotification());
        clearanceLimitWarningSent = true;
      } else if (dist < overNavaidDistance) {
        if (plane.isArrival() == false) {
          Navaid n = plane.getAssignedRoute().getMainNavaid();
          dist = Coordinates.getDistanceInNM(plane.getCoordinate(), n.getCoordinate());
          if (dist < 1.5) {
            int rad = (int) Coordinates.getBearing(LAcc.getAirport().getLocation(), n.getCoordinate());
            rad = rad % 90;
            plane.changePilot(
                new HoldPilot(plane, n, rad, true),
                Airplane.State.holding
            );
            return;
          }
        } else {
          plane.setTargetCoordinate(null);
          clearanceLimitWarningSent = false;
        }
      } else {
        double heading = Coordinates.getBearing(plane.getCoordinate(), targetCoordinate);
        heading = Headings.to(heading);
        if (heading != plane.getTargetHeading()) {
          plane.setTargetHeading(heading);
        }
      }
    }
    elapseSecondInternal();
  }

}
