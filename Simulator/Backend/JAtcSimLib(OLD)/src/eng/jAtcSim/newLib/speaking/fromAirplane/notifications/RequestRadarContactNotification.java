package eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.area.speaking.fromAirplane.IAirplaneNotification;

public class RequestRadarContactNotification implements IAirplaneNotification {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
