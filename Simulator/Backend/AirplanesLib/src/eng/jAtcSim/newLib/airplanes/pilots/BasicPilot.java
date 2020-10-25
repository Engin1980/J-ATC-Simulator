package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PassingClearanceLimitNotification;

public abstract class BasicPilot extends Pilot {

  private boolean clearanceLimitWarningSent = false;

  public BasicPilot(Airplane plane) {
    super(plane);
  }

  @Override
  public final void elapseSecondInternal() {
    Coordinate targetCoordinate = rdr.getRouting().tryGetTargetCoordinate();
    if (targetCoordinate != null) {

      double warningDistance = rdr.getSha().getSpeed() * .02;
      double overNavaidDistance = Navaid.getOverNavaidDistance(rdr.getSha().getSpeed());

      double dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), targetCoordinate);
      if (!clearanceLimitWarningSent
          && dist < warningDistance
          && !rdr.getRouting().hasLateralDirectionAfterCoordinate()) {
        wrt.sendMessage(
            new PassingClearanceLimitNotification());
        clearanceLimitWarningSent = true;
      } else if (dist < overNavaidDistance) {
        if (rdr.isArrival() == false) {
          Navaid n = rdr.getRouting().getEntryExitPoint();
          dist = Coordinates.getDistanceInNM(rdr.getCoordinate(), n.getCoordinate());
          if (dist < 1.5) {
            int rad = (int) Coordinates.getBearing(Context.getArea().getAirport().getLocation(), n.getCoordinate());
            rad = rad % 90;
            wrt.startHolding(n, rad, LeftRight.left);
            return;
          }
        } else {
          wrt.setTargetCoordinate(null);
          clearanceLimitWarningSent = false;
        }
      }
      // TODEL - done already in navigator
//      else {
//        double heading = Coordinates.getBearing(rdr.getCoordinate(), targetCoordinate);
//        heading = Headings.to(heading);
//        if (heading != rdr.getSha().getTargetHeading()) {
//          wrt.setTargetHeading(new HeadingNavigator(heading, LeftRightAny.any));
//        }
//      }
    }
    elapseSecondInternalBasic();
  }

  @Override
  public final boolean isDivertable() {
    return true;
  }

  protected abstract void elapseSecondInternalBasic();

}
