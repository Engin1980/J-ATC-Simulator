package eng.jAtcSim.newLib.airplanes.pilots;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import exml.XContext;
import exml.annotations.XConstructor;

public class DeparturePilot extends BasicPilot {

  @XConstructor
  private DeparturePilot(XContext ctx) {
    super(ctx);
  }

  public DeparturePilot(Airplane plane) {
    super(plane);
  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.takeOff,
            AirplaneState.departingLow,
            AirplaneState.departingHigh
    };
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return new AirplaneState[]{
            AirplaneState.departingLow,
            AirplaneState.departingHigh
    };
  }

  @Override
  protected void elapseSecondInternalBasic() {
    switch (rdr.getState()) {
      case departingLow:
        if (rdr.getSha().getAltitude() > 10000)
          wrt.setState(AirplaneState.departingHigh);
        break;
      case departingHigh:
        break;
      default:
        super.throwIllegalStateException();
    }
  }

}
