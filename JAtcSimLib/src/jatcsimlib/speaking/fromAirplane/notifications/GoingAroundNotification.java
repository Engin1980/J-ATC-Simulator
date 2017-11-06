package jatcsimlib.speaking.fromAirplane.notifications;

import jatcsimlib.speaking.fromAirplane.IAirplaneNotification;

public class GoingAroundNotification implements IAirplaneNotification {

  private String reason;

  public GoingAroundNotification(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "Going around due to " + reason + " {notification}";

    return ret;
  }
}
