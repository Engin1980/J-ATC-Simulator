package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;

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
