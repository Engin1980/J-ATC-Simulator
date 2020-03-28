package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.speeches.INotification;

/**
 * Confirmation of radar contact from ATC to plane.
 */
public class RadarContactConfirmationNotification implements INotification {

  @Override
  public String toString(){
    String ret = "Radar contact confirmation. {notification}";

    return ret;
  }
}
