package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public class Rejection extends CommandResponse {

  //TODO reason should be some kind of notification?
  public String reason;

  public Rejection(String reason, IAtcCommand origin) {
    super(origin);
    this.reason = reason;
  }

  /**
   * Reason of the messageType.
   * @return
   */
  public String getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "Rejection of |:" + this.getOrigin().toString() + ":| due to " + this.getReason() + " {notification}";
    return ret;
  }

}
