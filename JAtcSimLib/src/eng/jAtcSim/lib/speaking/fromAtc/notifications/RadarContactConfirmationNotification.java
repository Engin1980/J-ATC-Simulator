package eng.jAtcSim.lib.speaking.fromAtc.notifications;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcNotification;

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
