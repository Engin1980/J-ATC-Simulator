package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;

public class ApproachErrorCondition {
  public static ApproachErrorCondition create(ICondition condition, GoingAroundNotification.GoAroundReason goAroundReason) {
    return new ApproachErrorCondition(condition, goAroundReason);
  }

  public static ISet<ApproachErrorCondition> createSet(
          ICondition condition1, GoingAroundNotification.GoAroundReason goAroundReason1) {
    ISet<ApproachErrorCondition> ret = new ESet<>();
    ret.add(new ApproachErrorCondition(condition1, goAroundReason1));
    return ret;
  }

  public static ISet<ApproachErrorCondition> createSet(
          ICondition condition1, GoingAroundNotification.GoAroundReason goAroundReason1,
          ICondition condition2, GoingAroundNotification.GoAroundReason goAroundReason2) {
    ISet<ApproachErrorCondition> ret = new ESet<>();
    ret.add(new ApproachErrorCondition(condition1, goAroundReason1));
    ret.add(new ApproachErrorCondition(condition2, goAroundReason2));
    return ret;
  }

  public static ISet<ApproachErrorCondition> createSet(
          ICondition condition1, GoingAroundNotification.GoAroundReason goAroundReason1,
          ICondition condition2, GoingAroundNotification.GoAroundReason goAroundReason2,
          ICondition condition3, GoingAroundNotification.GoAroundReason goAroundReason3,
          ICondition condition4, GoingAroundNotification.GoAroundReason goAroundReason4) {
    ISet<ApproachErrorCondition> ret = new ESet<>();
    ret.add(new ApproachErrorCondition(condition1, goAroundReason1));
    ret.add(new ApproachErrorCondition(condition1, goAroundReason1));
    ret.add(new ApproachErrorCondition(condition2, goAroundReason2));
    ret.add(new ApproachErrorCondition(condition3, goAroundReason3));
    ret.add(new ApproachErrorCondition(condition4, goAroundReason4));
    return ret;
  }

  public static ISet<ApproachErrorCondition> createSet(
          ICondition condition1, GoingAroundNotification.GoAroundReason goAroundReason1,
          ICondition condition2, GoingAroundNotification.GoAroundReason goAroundReason2,
          ICondition condition3, GoingAroundNotification.GoAroundReason goAroundReason3) {
    ISet<ApproachErrorCondition> ret = new ESet<>();
    ret.add(new ApproachErrorCondition(condition1, goAroundReason1));
    ret.add(new ApproachErrorCondition(condition2, goAroundReason2));
    ret.add(new ApproachErrorCondition(condition3, goAroundReason3));
    return ret;
  }
  private final ICondition condition;
  private final GoingAroundNotification.GoAroundReason goAroundReason;

  public ApproachErrorCondition(ICondition condition, GoingAroundNotification.GoAroundReason goAroundReason) {
    this.condition = condition;
    this.goAroundReason = goAroundReason;
  }

  public ICondition getCondition() {
    return condition;
  }

  public GoingAroundNotification.GoAroundReason getGoAroundReason() {
    return goAroundReason;
  }
}