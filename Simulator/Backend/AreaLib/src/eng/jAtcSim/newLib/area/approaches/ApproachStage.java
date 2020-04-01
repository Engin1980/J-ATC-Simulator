package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;

public class ApproachStage {
  private final ICondition exitCondition;
  private final ICondition errorCondition;
  private final IApproachBehavior behavior;
  private final String name;

  public ApproachStage(IApproachBehavior behavior, ICondition exitCondition, ICondition errorCondition) {
    this(behavior, exitCondition, errorCondition, "Unnamed stage");
  }

  public ApproachStage(IApproachBehavior behavior, ICondition exitCondition, ICondition errorCondition, String name) {
    EAssert.Argument.isNotNull(behavior, "behavior");
    this.exitCondition = exitCondition;
    this.errorCondition = errorCondition;
    this.behavior = behavior;
    this.name = name;
  }

  public IApproachBehavior getBehavior() {
    return behavior;
  }

  public ICondition getErrorCondition() {
    return errorCondition;
  }

  public ICondition getExitCondition() {
    return exitCondition;
  }
}
