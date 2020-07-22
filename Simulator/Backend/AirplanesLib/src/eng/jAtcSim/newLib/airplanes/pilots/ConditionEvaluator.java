package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.weather.Weather;

public class ConditionEvaluator {

  public static boolean check(ICondition condition, IAirplane plane) {
    if (condition instanceof AggregatingCondition)
      return checkTrue((AggregatingCondition) condition, plane);
    else if (condition instanceof FlyRouteBehaviorEmptyCondition)
      return checkTrue((FlyRouteBehaviorEmptyCondition) condition, plane);
    else if (condition instanceof LocationCondition)
      return checkTrue((LocationCondition) condition, plane);
    else if (condition instanceof NegationCondition)
      return checkTrue((NegationCondition) condition, plane);
    else if (condition instanceof PlaneOrderedAltitudeDifferenceCondition)
      return checkTrue((PlaneOrderedAltitudeDifferenceCondition) condition, plane);
    else if (condition instanceof PlaneShaCondition)
      return checkTrue((PlaneShaCondition) condition, plane);
    else if (condition instanceof RunwayThresholdVisibilityCondition)
      return checkTrue((RunwayThresholdVisibilityCondition) condition, plane);
    else
      throw new UnsupportedOperationException("Unknown condition type.");
  }

  private static boolean checkTrue(RunwayThresholdVisibilityCondition condition, IAirplane plane){
    Weather w = Context.getWeather().getWeather();
    if (w.getCloudBaseInFt() > plane.getSha().getAltitude())
      return true;
    else
      return Context.getApp().getRnd().nextDouble() > w.getCloudBaseHitProbability();
  }

  private static boolean checkTrue(FlyRouteBehaviorEmptyCondition condition, IAirplane plane){
    return plane.getRouting().isRoutingEmpty();
  }

  private static boolean checkTrue(PlaneShaCondition condition, IAirplane plane) {
    char c = plane.getType().category;
    if (condition.getMinAltitude() != null && condition.getMinAltitude().get(c) > plane.getSha().getAltitude())
      return false;
    if (condition.getMaxAltitude() != null && condition.getMaxAltitude().get(c) < plane.getSha().getAltitude())
      return false;
    if (condition.getMinSpeed() != null && condition.getMinSpeed().get(c) > plane.getSha().getSpeed())
      return false;
    if (condition.getMaxSpeed() != null && condition.getMaxSpeed().get(c) < plane.getSha().getSpeed())
      return false;
    if (condition.getMinHeading() != null && condition.getMaxHeading() != null &&
        Headings.isBetween(condition.getMinHeading().get(c), plane.getSha().getHeading(), condition.getMaxHeading().get(c)))
      return false;
    return true;
  }

  private static boolean checkTrue(PlaneOrderedAltitudeDifferenceCondition condition, IAirplane plane) {
    int diff = plane.getSha().getTargetAltitude() - plane.getSha().getAltitude();
    return condition.getActualMinusTargetAltitudeMaximalDifference().get(plane.getType().category) > diff;
  }

  private static boolean checkTrue(AggregatingCondition condition, IAirplane plane) {
    switch (condition.getAggregator()) {
      case and:
        return condition.getConditions().isAll(q -> check(q, plane));
      case or:
        return condition.getConditions().isAny(q -> check(q, plane));
      default:
        throw new EEnumValueUnsupportedException(condition.getAggregator());
    }
  }

  private static boolean checkTrue(LocationCondition condition, IAirplane plane) {
    return condition.getLocation().isInside(plane.getCoordinate());
  }

  private static boolean checkTrue(NegationCondition condition, IAirplane plane) {
    return !check(condition, plane);
  }
}
