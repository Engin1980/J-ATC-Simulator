package eng.jAtcSim.newLib.speeches.airplane2atc;


import eng.jAtcSim.newLib.speeches.INotification;

public class RequestRadarContactNotification implements INotification {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
