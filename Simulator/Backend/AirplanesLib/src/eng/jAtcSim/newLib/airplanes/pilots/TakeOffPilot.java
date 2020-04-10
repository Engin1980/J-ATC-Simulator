package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;

public class TakeOffPilot extends Pilot {
  //TODO add to airport config the acceleration altitude and use it here
  private ActiveRunwayThreshold toThreshold;

  public TakeOffPilot(IPilotsPlane plane, ActiveRunwayThreshold takeOffThreshold) {
    super(plane);
    EAssert.Argument.isNotNull(takeOffThreshold, "takeOffThreshold");
    this.toThreshold = takeOffThreshold;
  }

  @Override
  public void elapseSecond() {
    switch (plane.getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            plane.getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
        plane.setTargetHeading(targetHeading);

        if (plane.getSpeed() > plane.getType().vR) {
          plane.setState(Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (plane.getAltitude() > this.toThreshold.getAccelerationAltitude())
          if (plane.isArrival()) {
            // antecedent G/A
            plane.changePilot(new ArrivalPilot(plane), Airplane.State.arrivingHigh);
          } else {
            plane.changePilot(
                new DeparturePilot(super.plane),
                Airplane.State.departingLow
            );
          }
        break;
      default:
        super.throwIllegalStateException();
    }
  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
