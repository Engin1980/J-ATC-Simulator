package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses;

import jatcsimlib.speaking.fromAirplane.IAirplaneNotification;

public class IllegalThenCommandRejection implements IAirplaneNotification {

  private final String reason;

  public IllegalThenCommandRejection(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "{then} rejection due to " + this.reason;
    return ret;
  }
}
