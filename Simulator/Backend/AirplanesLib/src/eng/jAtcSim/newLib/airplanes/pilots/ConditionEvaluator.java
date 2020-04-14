package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;
import eng.jAtcSim.newLib.weather.Weather;

public class ConditionEvaluator {

  public static boolean check(ICondition condition, IPilotPlane plane) {
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

  private static boolean checkTrue(RunwayThresholdVisibilityCondition condition, IPilotPlane plane){
    Weather w = LAcc.getWeather();
    if (w.getCloudBaseInFt() > plane.getAltitude())
      return true;
    else
      return GAcc.getRnd().nextDouble() > w.getCloudBaseHitProbability();
  }

  private static boolean checkTrue(FlyRouteBehaviorEmptyCondition condition, IPilotPlane plane){
    return plane.isRoutingEmpty();
  }

  private static boolean checkTrue(PlaneShaCondition condition, IPilotPlane plane) {
    char c = plane.getType().category;
    if (condition.getMinAltitude() != null && condition.getMinAltitude().get(c) > plane.getAltitude())
      return false;
    if (condition.getMaxAltitude() != null && condition.getMaxAltitude().get(c) < plane.getAltitude())
      return false;
    if (condition.getMinSpeed() != null && condition.getMinSpeed().get(c) > plane.getSpeed())
      return false;
    if (condition.getMaxSpeed() != null && condition.getMaxSpeed().get(c) < plane.getSpeed())
      return false;
    if (condition.getMinHeading() != null && condition.getMaxHeading() != null &&
        Headings.isBetween(condition.getMinHeading().get(c), plane.getHeading(), condition.getMaxHeading().get(c)))
      return false;
    return true;
  }

  private static boolean checkTrue(PlaneOrderedAltitudeDifferenceCondition condition, IPilotPlane plane) {
    int diff = plane.getTargetAltitude() - plane.getAltitude();
    return condition.getActualMinusTargetAltitudeMaximalDifference().get(plane.getType().category) > diff;
  }

  private static boolean checkTrue(AggregatingCondition condition, IPilotPlane plane) {
    switch (condition.getAggregator()) {
      case and:
        return condition.getConditions().isAll(q -> check(q, plane));
      case or:
        return condition.getConditions().isAny(q -> check(q, plane));
      default:
        throw new EEnumValueUnsupportedException(condition.getAggregator());
    }
  }

  private static boolean checkTrue(LocationCondition condition, IPilotPlane plane) {
    return condition.getLocation().isInside(plane.getCoordinate());
  }

  private static boolean checkTrue(NegationCondition condition, IPilotPlane plane) {
    return !check(condition, plane);
  }
}
