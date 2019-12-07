package eng.jAtcSim.newLib.speaking.fromAtc.notifications;

import eng.jAtcSim.newLib.speaking.fromAtc.IAtcNotification;

/**
 * Confirmation of radar contact from ATC to plane.
 */
public class RadarContactConfirmationNotification implements IAtcNotification {

  @Override
  public String toString(){
    String ret = "Radar contact confirmation. {notification}";

    return ret;
  }
}
