package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class DeparturePilot extends BasicPilot {
  public DeparturePilot(Airplane plane) {
    super(plane);
  }

  @Override
  protected void _save(XElement target) {
    // nothing to save
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
