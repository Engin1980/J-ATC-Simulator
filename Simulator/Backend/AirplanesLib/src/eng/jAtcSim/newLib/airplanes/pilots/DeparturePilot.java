package eng.jAtcSim.newLib.airplanes.pilots;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;

public class DeparturePilot extends BasicPilot {
  public DeparturePilot(IPlaneInterface plane) {
    super(plane);
  }

  @Override
  protected Airplane.State[] getInitialStates() {
    return new Airplane.State[]{
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh
    };
  }

  @Override
  protected Airplane.State[] getValidStates() {
    return new Airplane.State[]{
        Airplane.State.departingLow,
        Airplane.State.departingHigh
    };
  }

  @Override
  protected void elapseSecondInternalBasic() {
    switch (plane.getState()) {
      case departingLow:
        if (plane.getAltitude() > 10000)
          plane.setState(Airplane.State.departingHigh);
        break;
      case departingHigh:
        break;
      default:
        super.throwIllegalStateException();
    }
  }

}
