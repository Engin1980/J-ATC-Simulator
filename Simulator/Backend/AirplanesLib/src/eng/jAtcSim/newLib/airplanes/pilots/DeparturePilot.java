package eng.jAtcSim.newLib.airplanes.pilots;

import eng.jAtcSim.newLib.airplanes.Airplane;

public class DeparturePilot extends BasicPilot {
  public DeparturePilot(IPilotPlane plane) {
    super(plane);
  }

  @Override
  protected void elapseSecondInternal() {
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
