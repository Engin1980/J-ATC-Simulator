package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import jatcsimlib.speaking.fromAtc.commands.ClearedToApproachCommand;

public class UnableToEnterApproachFromDifficultPosition extends Rejection {
  public UnableToEnterApproachFromDifficultPosition(ClearedToApproachCommand origin) {
    super(
        "Cannot enter approach from current position.",
        origin);
  }
  @Override
  public String toString(){
    String ret = "Rejection of approach clearance. " + super.toString();

    return ret;
  }
}
