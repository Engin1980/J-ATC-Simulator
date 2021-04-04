package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.eSystem.utilites.EnumUtils;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;

public class GoingAroundNotification implements IFromPlaneSpeech {

  public enum GoAroundReason {
    decisionPointRunwayNotInSight,
    incorrectApproachEnter,
    unstabilizedAltitude,
    unstabilizedHeading,
    unstabilizedRadial,
    notOnTowerAtc,
    //    notStabilizedAirplane,
    //    noLandingClearance,
    //    windGustBeforeTouchdown,
    unknownUnusedProbablyBut__lostTrafficSeparationInApproach,
    orderedByAtc;

    private static final GoAroundReason[] ATC_FAIL_REASONS = {
            GoingAroundNotification.GoAroundReason.unknownUnusedProbablyBut__lostTrafficSeparationInApproach,
            GoingAroundNotification.GoAroundReason.notOnTowerAtc,
            GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
            GoingAroundNotification.GoAroundReason.unstabilizedAltitude,
            GoingAroundNotification.GoAroundReason.unstabilizedHeading
    };

    public boolean isAtcFail() {
      return EnumUtils.is(this, ATC_FAIL_REASONS);
    }
  }

  public GoAroundReason reason;

  public GoingAroundNotification(GoAroundReason reason) {
    this.reason = reason;
  }

  public GoAroundReason getReason() {
    return reason;
  }

  @Override
  public String toString() {
    String ret = "Going around due to " + reason + " {notification}";

    return ret;
  }
}
