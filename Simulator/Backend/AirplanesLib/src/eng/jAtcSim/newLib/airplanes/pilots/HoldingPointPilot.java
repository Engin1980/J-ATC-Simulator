package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;

public class HoldingPointPilot extends Pilot {
  public HoldingPointPilot(Airplane plane) {
    super(plane);
  }

  @XConstructor
  private HoldingPointPilot(XLoadContext ctx) {
    super(ctx);
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
