package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.geo.Coordinates;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;

public class TakeOffPilot extends Pilot {
  //TODO add to airport config the acceleration altitude and use it here
  private final ActiveRunwayThreshold takeOffThreshold;

  public TakeOffPilot(Airplane plane, ActiveRunwayThreshold takeOffThreshold) {
    super(plane);
    EAssert.Argument.isNotNull(takeOffThreshold, "takeOffThreshold");
    this.takeOffThreshold = takeOffThreshold;
  }

  @Override
  public void elapseSecondInternal() {
    switch (rdr.getState()) {
      case takeOffRoll:
        double targetHeading = Coordinates.getBearing(
            rdr.getCoordinate(), takeOffThreshold.getOtherThreshold().getCoordinate());
        wrt.setTargetHeading(new HeadingNavigator(targetHeading, LeftRightAny.any));

        if (rdr.getSha().getSpeed() > rdr.getType().vR) {
          wrt.setState(AirplaneState.takeOffGoAround);
        }
        break;
      case takeOffGoAround:
        // keeps last heading
        // altitude already set
        // speed set
        if (rdr.getSha().getAltitude() > this.takeOffThreshold.getAccelerationAltitude())
          if (rdr.isArrival()) {
            // antecedent G/A
            wrt.startArriving();
          } else {
            wrt.startDeparting();
          }
        break;
      default:
        super.throwIllegalStateException();
    }
  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
        AirplaneState.takeOffRoll
    };
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround
    };
  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
