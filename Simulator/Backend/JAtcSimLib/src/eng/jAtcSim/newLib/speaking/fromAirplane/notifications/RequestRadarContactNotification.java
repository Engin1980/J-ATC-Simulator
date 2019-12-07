package eng.jAtcSim.newLib.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.speaking.fromAirplane.IAirplaneNotification;

public class RequestRadarContactNotification implements IAirplaneNotification {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
