package eng.jAtcSim.newLib.airplanes.pilots;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;

public class HoldingPointPilot extends Pilot {
  public HoldingPointPilot(IPlaneInterface plane) {
    super(plane);
  }

  @Override
  public void elapseSecondInternal() {

  }

  @Override
  protected Airplane.State[] getInitialStates() {
    return new Airplane.State[]{Airplane.State.holdingPoint};
  }

  @Override
  protected Airplane.State[] getValidStates() {
    return getInitialStates();
  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
