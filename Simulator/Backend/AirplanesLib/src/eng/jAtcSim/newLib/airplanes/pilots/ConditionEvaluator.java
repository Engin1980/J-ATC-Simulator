package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.conditions.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.weather.Weather;

public class ConditionEvaluator {

  public static boolean check(ICondition condition, IAirplane plane) {
    boolean ret;
    if (condition instanceof AggregatingCondition)
      ret = checkTrue((AggregatingCondition) condition, plane);
    else if (condition instanceof FlyRouteBehaviorEmptyCondition)
      ret = checkTrue((FlyRouteBehaviorEmptyCondition) condition, plane);
    else if (condition instanceof ILocation)
      ret = checkTrue((ILocation) condition, plane);
    else if (condition instanceof NegationCondition)
      ret = checkTrue((NegationCondition) condition, plane);
    else if (condition instanceof PlaneOrderedAltitudeDifferenceCondition)
      ret = checkTrue((PlaneOrderedAltitudeDifferenceCondition) condition, plane);
    else if (condition instanceof PlaneShaCondition)
      ret = checkTrue((PlaneShaCondition) condition, plane);
    else if (condition instanceof RunwayThresholdVisibleCondition)
      ret = checkTrue((RunwayThresholdVisibleCondition) condition, plane);
    else if (condition instanceof NeverCondition)
      ret = false;
    else
      throw new UnsupportedOperationException("Unknown condition type.");

    return ret;
  }

  private static boolean checkTrue(RunwayThresholdVisibleCondition condition, IAirplane plane) {
    Weather w = Context.getWeather().getWeather();
    if (w.getVisibilityInMeters() < 9999) {
      double dist = Coordinates.getDistanceInKm(
              plane.getCoordinate(),
              plane.getRouting().getAssignedRunwayThreshold().getCoordinate());
      if (dist > w.getVisibilityInMeters())
        return false;
    }
    if (w.getCloudBaseInFt() > plane.getSha().getAltitude())
      return true;
    else
      return Context.getApp().getRnd().nextDouble() > w.getCloudBaseHitProbability();
  }

  private static boolean checkTrue(FlyRouteBehaviorEmptyCondition condition, IAirplane plane) {
    return plane.getRouting().isRoutingEmpty();
  }

  private static boolean checkTrue(PlaneShaCondition condition, IAirplane plane) {
    boolean ret;
    char c = plane.getType().category;

    if (condition.getType() == PlaneShaCondition.eType.heading)
      ret = Headings.isBetween(condition.getMinimum().get(c), plane.getSha().getHeading(), condition.getMaximum().get(c));
    else {
      EAssert.isTrue(condition.getType() == PlaneShaCondition.eType.altitude || condition.getType() == PlaneShaCondition.eType.speed);
      double currentValue = condition.getType() == PlaneShaCondition.eType.altitude ?
              plane.getSha().getAltitude() : plane.getSha().getSpeed();
      ret = (condition.getMinimum() == null || condition.getMinimum().get(c) < currentValue)
              &&
              (condition.getMaximum() == null || condition.getMaximum().get(c) > currentValue);
    }

    return ret;
  }

  private static boolean checkTrue(PlaneOrderedAltitudeDifferenceCondition condition, IAirplane plane) {
    int diff = Math.abs(plane.getSha().getTargetAltitude() - plane.getSha().getAltitude());
    char category = plane.getType().category;
    IntegerPerCategoryValue below = condition.tryGetMaximumBelowTargetAltitude();
    IntegerPerCategoryValue above = condition.tryGetMaximumAboveTargetAltitude();
    boolean ret = (below != null && below.get(category) < diff) || (above != null && above.get(category) < diff);
    return ret;
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

  private static boolean checkTrue(ILocation condition, IAirplane plane) {
    return condition.isInside(plane.getCoordinate());
  }

  private static boolean checkTrue(NegationCondition condition, IAirplane plane) {
    return !check(condition, plane);
  }
}
