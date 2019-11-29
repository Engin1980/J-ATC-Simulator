package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;

public class RequestRadarContactNotification implements IAirplaneNotification {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
