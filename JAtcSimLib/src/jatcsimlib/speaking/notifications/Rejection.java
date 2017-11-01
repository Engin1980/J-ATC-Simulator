package jatcsimlib.speaking.notifications;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandResponse;

public class Rejection extends CommandResponse {

  public String reason;

  public Rejection(String reason, Command origin) {
    super(origin);
    this.reason = reason;
  }

  /**
   * Reason of the rejection.
   * @return
   */
  public String getReason() {
    return reason;
  }

}
