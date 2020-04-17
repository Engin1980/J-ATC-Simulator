package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;

public class TakeOffPilot extends Pilot {
  //TODO add to airport config the acceleration altitude and use it here
  private final ActiveRunwayThreshold takeOffThreshold;

  public TakeOffPilot(IPlaneInterface plane, ActiveRunwayThreshold takeOffThreshold) {
    super(plane);
    EAssert.Argument.isNotNull(takeOffThreshold, "takeOffThreshold");
    this.takeOffThreshold = takeOffThreshold;
  }

  @Override
  public void elapseSecondInternal() {
    switch (plane.getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            plane.getCoordinate(), takeOffThreshold.getOtherThreshold().getCoordinate());
        plane.setTargetHeading(new HeadingNavigator(targetHeading, LeftRightAny.any));

        if (plane.getSpeed() > plane.getType().vR) {
          plane.setState(Airplane.State.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (plane.getAltitude() > this.takeOffThreshold.getAccelerationAltitude())
          if (plane.isArrival()) {
            // antecedent G/A
            plane.startArriving();
          } else {
            plane.startDeparting();
          }
        break;
      default:
        super.throwIllegalStateException();
    }
  }

  @Override
  protected Airplane.State[] getInitialStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint
    };
  }

  @Override
  protected Airplane.State[] getValidStates() {
    return new Airplane.State[]{
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround
    };
  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
