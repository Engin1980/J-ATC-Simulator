package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class HoldingPointPilot extends Pilot {
  public HoldingPointPilot(IPilotsPlane plane) {
    super(plane);
  }

  @Override
  public void elapseSecond() {

  }

  @Override
  public boolean isDivertable() {
    return false;
  }
}
