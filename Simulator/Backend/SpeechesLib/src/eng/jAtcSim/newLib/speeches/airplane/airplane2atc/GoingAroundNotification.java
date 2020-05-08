package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;

public class GoingAroundNotification implements IFromPlaneSpeech {

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
