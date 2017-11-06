package jatcsimlib.speaking.fromAirplane.notifications.commandResponses.rejections;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import jatcsimlib.speaking.fromAtc.commands.ClearedToApproachCommand;

public class UnableToEnterApproachFromDifficultPosition extends Rejection {
  public UnableToEnterApproachFromDifficultPosition(ClearedToApproachCommand origin) {
    super(
        "Cannot enter approach now. Difficult position.",
        origin);
  }
  @Override
  public String toString(){
    String ret = "Rejection of approach clearance. " + super.toString();

    return ret;
  }
}
