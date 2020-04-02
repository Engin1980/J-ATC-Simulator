package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;

public class NegationCondition implements ICondition {

  public static NegationCondition create(ICondition condition) {
    return new NegationCondition(condition);
  }
  private final ICondition condition;

  private NegationCondition(ICondition condition) {
    EAssert.Argument.isNotNull(condition, "condition");
    this.condition = condition;
  }

  public ICondition getCondition() {
    return condition;
  }
}
