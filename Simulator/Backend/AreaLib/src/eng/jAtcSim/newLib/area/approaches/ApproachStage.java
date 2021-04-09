package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.IApproachBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.ICondition;
import eng.jAtcSim.newLib.shared.PostContracts;

import exml.IXPersistable;
import exml.loading.XLoadContext; import exml.saving.XSaveContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class ApproachStage implements IXPersistable {
  public static ApproachStage create(String name, IApproachBehavior behavior, ICondition exitCondition, ApproachErrorCondition... errorCondition) {
    ESet<ApproachErrorCondition> set = ESet.of(errorCondition);
    return new ApproachStage(behavior, exitCondition, set, name);
  }

  public static ApproachStage create(String name, IApproachBehavior behavior, ICondition exitCondition, ISet<ApproachErrorCondition> errorConditions) {
    return new ApproachStage(behavior, exitCondition, errorConditions, name);
  }

  private final ICondition exitCondition;
  private final ISet<ApproachErrorCondition> errorConditions;
  private final IApproachBehavior behavior;
  private final String name;


  @XConstructor
  private ApproachStage() {
    this.exitCondition = null;
    this.errorConditions = null;
    this.behavior = null;
    this.name = null;

    PostContracts.register(this, () -> this.behavior != null);
    PostContracts.register(this, () -> this.exitCondition != null);
    PostContracts.register(this, () -> this.errorConditions != null);
  }

  private ApproachStage(IApproachBehavior behavior, ICondition exitCondition, ISet<ApproachErrorCondition> errorConditions, String name) {
    EAssert.Argument.isNotNull(behavior, "behavior");
    EAssert.Argument.isNotNull(exitCondition, "exitCondition");
    EAssert.Argument.isNotNull(errorConditions, "errorConditions");

    this.exitCondition = exitCondition;
    this.errorConditions = errorConditions;
    this.behavior = behavior;
    this.name = name;
  }

  public IApproachBehavior getBehavior() {
    return behavior;
  }

  public ISet<ApproachErrorCondition> getErrorConditions() {
    return errorConditions;
  }

  public ICondition getExitCondition() {
    return exitCondition;
  }

  public String getName() {
    return name;
  }

  @Override
  public void xLoad(XElement elm, XLoadContext ctx) {
    ctx.fields.loadFieldItems(this, "errorConditions", new ESet<>(), ApproachErrorCondition.class, elm);
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    ctx.fields.saveFieldItems(this, "errorConditions",
            ApproachErrorCondition.class, elm);
  }
}
