package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;

/**
 * Confirmation of radar contact from ATC to plane.
 */
public class RadarContactConfirmationNotification implements IForPlaneSpeech {

  @Override
  public String toString(){
    String ret = "Radar contact confirmation. {notification}";

    return ret;
  }
}
