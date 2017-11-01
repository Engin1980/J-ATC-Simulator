package jatcsimlib.speaking.notifications.specific;

import jatcsimlib.speaking.notifications.Notification;

public class GoingAroundNotification extends Notification {

  private String reason;

  public GoingAroundNotification(String reason) {
    this.reason = reason;
  }

  public String getReason() {
    return reason;
  }
}
