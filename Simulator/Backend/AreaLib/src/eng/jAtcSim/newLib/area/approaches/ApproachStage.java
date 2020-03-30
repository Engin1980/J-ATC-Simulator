package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ApproachStage {
  private final IList<ICondition> exitConditions;
  private final IList<ICondition> errorConditions;
  private final IApproachBehavior behavior;

  public ApproachStage( IApproachBehavior behavior, IList<ICondition> exitConditions, IList<ICondition> errorConditions) {
    EAssert.Argument.isNotNull(behavior, "behavior");
    EAssert.Argument.isNotNull(exitConditions, "exitConditions");
    EAssert.Argument.isTrue(!exitConditions.isEmpty(), "There must be at least one exit condition.");
    EAssert.Argument.isNotNull(errorConditions, "errorConditions");
    this.exitConditions = exitConditions;
    this.errorConditions = errorConditions;
    this.behavior = behavior;
  }

  public IList<ICondition> getExitConditions() {
    return exitConditions;
  }

  public IList<ICondition> getErrorConditions() {
    return errorConditions;
  }

  public IApproachBehavior getBehavior() {
    return behavior;
  }
}
