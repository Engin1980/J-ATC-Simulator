package eng.jAtcSim.newLib.airplanes.pilots;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

public class ApproachPilot extends Pilot {

  public ApproachPilot(IPilotPlane plane) {
    super(plane);
  }

  @Override
  public void elapseSecond() {
    throw new ToDoException();
  }

  @Override
  public boolean isDivertable() {
    return plane.getState() != Airplane.State.approachDescend;
  }
}
