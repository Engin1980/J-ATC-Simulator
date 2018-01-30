package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import jatcsimlib.speaking.fromAirplane.IAirplaneNotification;

public class EstablishedOnApproachNotification implements IAirplaneNotification {

  @Override
  public String toString(){
    String ret = "Established on approach {notification}";

    return ret;
  }

}
