package jatcsimlib.speaking.fromAirplane.notifications;

import jatcsimlib.speaking.fromAirplane.IAirplaneNotification;

public class RequestRadarContactNotification implements IAirplaneNotification {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
