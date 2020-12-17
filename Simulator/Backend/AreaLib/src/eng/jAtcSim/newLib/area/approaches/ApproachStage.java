package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IReadOnlySet;
import eng.eSystem.collections.ISet;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class ApproachStage {
  public static ApproachStage create(String name, IApproachBehavior behavior, ICondition exitCondition, ApproachErrorCondition ... errorCondition) {
    ESet<ApproachErrorCondition> set = new ESet<>(errorCondition);
    return new ApproachStage(behavior, exitCondition, set, name);
  }

  public static ApproachStage create(String name, IApproachBehavior behavior, ICondition exitCondition, ISet<ApproachErrorCondition> errorConditions) {
    return new ApproachStage(behavior, exitCondition, errorConditions, name);
  }

  private final ICondition exitCondition;
  private final ISet<ApproachErrorCondition> errorCondition;
  private final IApproachBehavior behavior;
  private final String name;

  @XmlConstructor
  private ApproachStage(){
    this.exitCondition = null;
    this.errorCondition = null;
    this.behavior = null;
    this.name = null;

    PostContracts.register(this, () -> this.behavior != null);
    PostContracts.register(this, () -> this.exitCondition != null);
    PostContracts.register(this, () -> this.errorCondition != null);
  }

  private ApproachStage(IApproachBehavior behavior, ICondition exitCondition, ISet<ApproachErrorCondition> errorConditions, String name) {
    EAssert.Argument.isNotNull(behavior, "behavior");
    EAssert.Argument.isNotNull(exitCondition, "exitCondition");
    EAssert.Argument.isNotNull(errorConditions, "errorConditions");

    this.exitCondition = exitCondition;
    this.errorCondition = errorConditions;
    this.behavior = behavior;
    this.name = name;
  }

  public IApproachBehavior getBehavior() {
    return behavior;
  }

  public ISet<ApproachErrorCondition> getErrorConditions() {
    return errorCondition;
  }

  public ICondition getExitCondition() {
    return exitCondition;
  }

  public String getName() {
    return name;
  }
}
