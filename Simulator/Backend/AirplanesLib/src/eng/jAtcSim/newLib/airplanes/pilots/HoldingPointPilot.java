package eng.jAtcSim.newLib.airplanes.pilots;

public class HoldingPointPilot extends Pilot {
  public HoldingPointPilot(IPilotPlane plane) {
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
