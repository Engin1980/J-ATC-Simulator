package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;

public class GoingAroundNotification implements IAirplaneNotification {

  public enum GoAroundReason{
    runwayNotInSight,
    notStabilizedApproachEnter,
    notStabilizedOnFinal,
    noLandingClearance,
    windGustBeforeTouchdown,
    lostTrafficSeparationInApproach

  }

  public GoAroundReason reason;

  public GoingAroundNotification(GoAroundReason reason) {
    this.reason = reason;
  }

  public GoAroundReason getReason() {
    return reason;
  }

  @Override
  public String toString(){
    String ret = "Going around due to " + reason + " {notification}";

    return ret;
  }
}
