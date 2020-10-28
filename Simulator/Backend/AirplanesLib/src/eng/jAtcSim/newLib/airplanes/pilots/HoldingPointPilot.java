package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class HoldingPointPilot extends Pilot {
  public HoldingPointPilot(Airplane plane) {
    super(plane);
  }

  @Override
  protected void _save(XElement target) {
  }

  @Override
  public void elapseSecondInternal() {

  }

  @Override
  protected AirplaneState[] getInitialStates() {
    return new AirplaneState[]{AirplaneState.holdingPoint};
  }

  @Override
  protected AirplaneState[] getValidStates() {
    return getInitialStates();
  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
