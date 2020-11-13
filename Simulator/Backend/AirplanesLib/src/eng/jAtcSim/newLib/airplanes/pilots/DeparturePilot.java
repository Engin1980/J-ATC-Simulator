package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;

public class DeparturePilot extends BasicPilot {
  public DeparturePilot(Airplane plane) {
    super(plane);
  }

  public static DeparturePilot load(XElement element, IMap<String, Object> context) {
    Airplane airplane = (Airplane) context.get("airplane");

    DeparturePilot ret = new DeparturePilot(airplane);

    return ret;
  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{
            AirplaneState.takeOffGoAround,
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
