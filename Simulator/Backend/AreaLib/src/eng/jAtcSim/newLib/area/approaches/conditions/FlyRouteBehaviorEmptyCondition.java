package eng.jAtcSim.newLib.area.approaches.conditions;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRouteBehavior;

public class FlyRouteBehaviorEmptyCondition implements ICondition {
  private final FlyRouteBehavior flyRouteBehavior;

  public FlyRouteBehaviorEmptyCondition(FlyRouteBehavior flyRouteBehavior) {
    EAssert.Argument.isNotNull(flyRouteBehavior, "flyRouteBehavior");
    this.flyRouteBehavior = flyRouteBehavior;
  }

  public FlyRouteBehavior getFlyRouteBehavior() {
    return flyRouteBehavior;
  }

  @Override
  public String toString() {
    return "FlyRouteBehaviorEmptyCondition{" +
            "cnt=" + flyRouteBehavior.getCommands().count() +
            '}';
  }
}
