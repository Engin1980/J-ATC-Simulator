package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ApproachPilot extends Pilot {

  public enum State{
    star,
    iaf2faf,
    approach
  }

  private State state;

  public ApproachPilot(IPilotsPlane plane) {
    super(plane);
    this.state = State.star;
  }

  @Override
  public void elapseSecond() {

  }

  @Override
  public boolean isDivertable() {
    return state != State.approach;
  }
}
