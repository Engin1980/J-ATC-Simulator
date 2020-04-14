package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRouteBehavior;

public class FlyRouteBehaviorEmptyCondition implements ICondition {
  private final FlyRouteBehavior flyRouteBehavior;

  public FlyRouteBehaviorEmptyCondition(FlyRouteBehavior flyRouteBehavior) {
    EAssert.Argument.isNotNull(flyRouteBehavior, "flyRouteBehavior");
    this.flyRouteBehavior = flyRouteBehavior;
  }

  @Override
  public ICondition createCopy() {
    return new FlyRouteBehaviorEmptyCondition(this.flyRouteBehavior.createCopy());
  }

  public FlyRouteBehavior getFlyRouteBehavior() {
    return flyRouteBehavior;
  }
}
