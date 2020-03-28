package eng.jAtcSim.newLib.speeches.airplane2atc.responses;

import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedToApproachCommand;

public class UnableToEnterApproachFromDifficultPosition extends Rejection {
  public UnableToEnterApproachFromDifficultPosition(ClearedToApproachCommand origin, String reason) {
    super(
        "Unable to enter approach from current position. " + reason,
        origin);
  }
  @Override
  public String toString(){
    String ret = "Rejection of approach clearance. " + super.toString();

    return ret;
  }
}
