package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;

public class RequestRadarContactNotification implements IFromPlaneSpeech {

  @Override
  public String toString(){
    String ret = "Re-request radar contact. {notification}";

    return ret;
  }
}
