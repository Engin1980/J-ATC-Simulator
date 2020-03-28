package eng.jAtcSim.newLib.speeches.airplane2atc;

import eng.jAtcSim.newLib.speeches.INotification;

public class GoingAroundNotification implements INotification {

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
