package eng.jAtcSim.newLib.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.speaking.fromAirplane.IAirplaneNotification;

public class GoingAroundNotification implements IAirplaneNotification {

  public enum GoAroundReason{
    runwayNotInSight,
    incorrectApproachEnter,
    notStabilizedAirplane,
    noLandingClearance,
    windGustBeforeTouchdown,
    lostTrafficSeparationInApproach,
    atcDecision
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
