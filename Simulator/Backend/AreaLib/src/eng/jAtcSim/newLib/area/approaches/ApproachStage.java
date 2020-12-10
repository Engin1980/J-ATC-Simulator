package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.newXmlUtils.annotations.XmlConstructor;

public class ApproachStage {
  public static ApproachStage create(String name, IApproachBehavior behavior, ICondition exitCondition, ICondition errorCondition) {
    return new ApproachStage(behavior, exitCondition, errorCondition, name);
  }

  private final ICondition exitCondition;
  private final ICondition errorCondition;
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

  private ApproachStage(IApproachBehavior behavior, ICondition exitCondition, ICondition errorCondition, String name) {
    EAssert.Argument.isNotNull(behavior, "behavior");
    EAssert.Argument.isNotNull(exitCondition, "exitCondition");
    EAssert.Argument.isNotNull(errorCondition, "errorCondition");

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

  public String getName() {
    return name;
  }
}
