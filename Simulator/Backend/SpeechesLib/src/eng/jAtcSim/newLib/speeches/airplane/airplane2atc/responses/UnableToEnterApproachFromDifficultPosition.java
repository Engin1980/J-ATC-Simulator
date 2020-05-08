package eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses;

import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToApproachCommand;

public class UnableToEnterApproachFromDifficultPosition extends PlaneRejection {
  public UnableToEnterApproachFromDifficultPosition(ClearedToApproachCommand origin, String reason) {
    super(
        origin,
        "Unable to enter approach from current position. " + reason);
  }

  @Override
  public String toString() {
    String ret = "Rejection of approach clearance. " + super.toString();

    return ret;
  }
}
